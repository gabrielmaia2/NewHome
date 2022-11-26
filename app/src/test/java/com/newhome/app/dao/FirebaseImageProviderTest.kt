package com.newhome.app.dao

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.google.android.gms.tasks.*
import com.google.firebase.storage.FirebaseStorage
import com.newhome.app.MockUtils
import com.newhome.app.R
import com.newhome.app.TestUtils
import com.newhome.app.dao.firebase.FirebaseImageProvider
import com.newhome.app.utils.Utils
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.BeforeClass

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseImageProviderTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    private lateinit var applicationContext: Context

    private lateinit var defaultBitmap: Bitmap
    private lateinit var nonDefaultBitmap: Bitmap

    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var provider: FirebaseImageProvider

    @Before
    fun setup() {
        applicationContext = MockUtils.applicationContext

        defaultBitmap = MockUtils.defaultBitmap
        nonDefaultBitmap = MockUtils.nonDefaultBitmap

        firebaseStorage = MockUtils.mockFirebaseStorage("path/valid1", "path/valid2")

        provider = FirebaseImageProvider(applicationContext, firebaseStorage)
    }

    @Test
    fun `verify get default bitmap cached`() = runTest {
        var bmp = provider.getDefaultBitmap()
        assertEquals(defaultBitmap, bmp)

        bmp = provider.getDefaultBitmap()
        assertEquals(defaultBitmap, bmp)

        bmp = provider.getDefaultBitmap()
        assertEquals(defaultBitmap, bmp)

        val res = applicationContext.resources
        val id = R.drawable.image_default

        verify(exactly = 1) {
            BitmapFactory.decodeResource(res, id)
        }
    }

    @Test
    fun `verify save image null bitmap`() = runTest {
        provider.saveImage("path/valid1", null).await()

        val imgRef = firebaseStorage.reference.child("path/valid1")
        coVerify(exactly = 0) { imgRef.putBytes(any()) }
    }

    @Test
    fun `verify save image default bitmap`() = runTest {
        val e = TestUtils.assertThrowsAsync<Exception> {
            provider.saveImage("path/valid1", defaultBitmap).await()
        }

        assertEquals("Trying to save the default bitmap on firebase.", e.message)
    }

    @Test
    fun `verify save image`() = runTest {
        provider.saveImage("path/valid1", nonDefaultBitmap).await()

        val sha256 = Utils.sha256(MockUtils.byteArray)

        // check path
        val imgRef = firebaseStorage.reference.child("path/valid1.jpg")
        coVerify(exactly = 1) {
            imgRef.putBytes(any(), withArg {
                // check sha256sum
                mockkStatic(TextUtils::class)
                every { TextUtils.isEmpty(any()) } answers { arg<String>(0).isEmpty() }
                val actual = it.getCustomMetadata("sha256sum")
                assertEquals(sha256, actual)
            })
        }
    }

    @Test
    fun `verify get image invalid path`() = runTest {
        TestUtils.assertThrowsAsync<Exception> { provider.getImage("path/invalid").await() }
    }

    @Test
    fun `verify get image cached`() = runTest {
        provider.cache = hashMapOf(
            Utils.sha256(MockUtils.byteArray) to MockUtils.byteArray
        )
        val image = provider.getImage("path/valid1").await()

        val imgRef = firebaseStorage.reference.child("path/valid1.jpg")
        coVerify(exactly = 0) { imgRef.getBytes(any()) }
        assertEquals(nonDefaultBitmap, image)
    }

    @Test
    fun `verify get image`() = runTest {
        val image = provider.getImage("path/valid1").await()

        // check getting data
        val imgRef = firebaseStorage.reference.child("path/valid1.jpg")
        coVerify(exactly = 1) { imgRef.getBytes(any()) }
        assertEquals(nonDefaultBitmap, image)

        // check caching
        val sha256 = Utils.sha256(MockUtils.byteArray)
        assertEquals(MockUtils.byteArray, provider.cache[sha256])
    }

    @Test
    fun `verify get image or default invalid path`() = runTest {
        val image = provider.getImageOrDefault("path/invalid").await()
        assertEquals(defaultBitmap, image)
    }

    @Test
    fun `verify get image or default`() = runTest {
        val image = provider.getImageOrDefault("path/valid1").await()

        val imgRef = firebaseStorage.reference.child("path/valid1.jpg")
        coVerify(exactly = 1) { imgRef.getBytes(any()) }
        assertEquals(nonDefaultBitmap, image)
    }

    @Test
    fun `verify remove image invalid path`() = runTest {
        val sha256 = Utils.sha256(MockUtils.byteArray)
        provider.cache = hashMapOf(
            sha256 to MockUtils.byteArray
        )

        // check delete
        provider.removeImage("path/invalid").await()

        // check caching
        assertEquals(MockUtils.byteArray, provider.cache[sha256])
    }

    @Test
    fun `verify remove image`() = runTest {
        val sha256 = Utils.sha256(MockUtils.byteArray)
        provider.cache = hashMapOf(
            sha256 to MockUtils.byteArray
        )

        // check delete
        provider.removeImage("path/valid1").await()
        val imgRef = firebaseStorage.reference.child("path/valid1.jpg")
        coVerify(exactly = 1) { imgRef.delete() }

        // check caching
        assertEquals(null, provider.cache[sha256])
    }
}
