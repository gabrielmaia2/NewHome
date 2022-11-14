package com.newhome.app.dao

import android.graphics.Bitmap
import com.google.android.gms.tasks.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.newhome.app.TaskUtils
import com.newhome.app.dao.firebase.FirebaseUsuarioProvider
import com.newhome.app.dto.NovoUsuario
import com.newhome.app.dto.UsuarioData
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseUsuarioProviderTest {
    companion object {
        private lateinit var defaultBitmap: Bitmap
        private lateinit var imageProvider: IImageProvider

        private lateinit var firestore: FirebaseFirestore

        private lateinit var userSnap: DocumentSnapshot
        private lateinit var userDoc: DocumentReference
        private lateinit var anyDoc: DocumentReference

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            defaultBitmap = mockk()
            imageProvider = mockk()

            Dispatchers.setMain(StandardTestDispatcher())

            val saveImageTask = CoroutineScope(Dispatchers.Main).async { println("aaa")}
            val removeImageTask = CoroutineScope(Dispatchers.Main).async { }
            val getImageTask = CoroutineScope(Dispatchers.Main).async { return@async defaultBitmap }

            coEvery { imageProvider.saveImage("usuarios/userid", any()) } returns saveImageTask
            coEvery { imageProvider.removeImage("usuarios/userid") } returns removeImageTask
            coEvery { imageProvider.getImageOrDefault("usuarios/userid") } returns getImageTask

            firestore = mockk()

            val setOptions = mockk<SetOptions>()
            mockkStatic(SetOptions::merge)
            every { SetOptions.merge() } returns setOptions

            userSnap = mockk()
            every { userSnap.id } returns "userid"
            every { userSnap.data } returns hashMapOf(
                "nome" to "username",
                "detalhes" to "details"
            ) as Map<String, Any>

            val userCollection = mockk<CollectionReference>()

            val collectionId = slot<String>()
            coEvery { firestore.collection(capture(collectionId)) } answers {
                if (collectionId.captured.equals("usuarios")) userCollection
                else throw Exception("Invalid collection")
            }

            userDoc = mockk()
            anyDoc = mockk()

            val userId = slot<String>()
            coEvery { userCollection.document(capture(userId)) } answers {
                if (userId.captured.equals("userid")) userDoc
                else anyDoc
            }

            coEvery { userDoc.get() } returns TaskUtils.createSuccessTask(userSnap)
            coEvery { anyDoc.get() } returns TaskUtils.createFailureTask(Exception())
            coEvery { userDoc.set(any()) } returns TaskUtils.createVoidSuccessTask()
            coEvery {
                userDoc.set(
                    any(),
                    SetOptions.merge()
                )
            } returns TaskUtils.createVoidSuccessTask()
            coEvery { anyDoc.set(any(), SetOptions.merge()) } returns TaskUtils.createFailureTask(
                Exception()
            )
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
        }
    }

    private lateinit var provider: FirebaseUsuarioProvider

    @Before
    fun setup() {
        provider = FirebaseUsuarioProvider(firestore, imageProvider)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `verify get user`() = runTest {
        val user = provider.getUser("userid").await()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { doc.get() }
        assertEquals(user.id, "userid")
        assertEquals(user.nome, "username")
        assertEquals(user.detalhes, "details")
    }

    @Test
    fun `verify get nonexistent user`() = runTest {
        var exceptionThrown = false
        try {
            provider.getUser("nonexistentid").await()
        } catch (e: Exception) {
            exceptionThrown = true
        }
        assertTrue("Exception was not thrown", exceptionThrown)
    }

    @Test
    fun `verify create user`() = runTest {
        val user = NovoUsuario(
            "userid",
            "username",
            "details",
            18
        )
        provider.createUser(user).await()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { doc.set(any()) }
    }

    @Test
    fun `verify update user`() = runTest {
        val user = UsuarioData(
            "userid",
            "username",
            "details"
        )
        provider.updateUser(user).await()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { doc.set(any(), SetOptions.merge()) }
    }

    @Test
    fun `verify update nonexistent user`() = runTest {
        val user = UsuarioData(
            "nonexistentid",
            "username",
            "details"
        )
        var exceptionThrown = false
        try {
            provider.updateUser(user).await()
        } catch (e: Exception) {
            exceptionThrown = true
        }
        assertTrue("Exception was not thrown", exceptionThrown)
    }

    @Test
    fun `verify delete user`() = runTest {
        // TODO implement
    }

    @Test
    fun `verify delete nonexistent user`() = runTest {
        // TODO implement
    }

    @Test
    fun `verify get user image`() = runTest {
        val image = provider.getUserImage("userid").await()
        coVerify(exactly = 1) { imageProvider.getImageOrDefault("usuarios/userid") }
        assertEquals(image, defaultBitmap)
    }

    @Test
    fun `verify set user image`() = runTest {
        provider.setUserImage("userid", defaultBitmap).await()
        coVerify(exactly = 1) { imageProvider.saveImage("usuarios/userid", defaultBitmap) }
    }
}
