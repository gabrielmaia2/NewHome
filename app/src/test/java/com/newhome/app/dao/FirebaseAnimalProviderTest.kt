package com.newhome.app.dao

import android.graphics.Bitmap
import com.google.android.gms.tasks.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.newhome.app.MockUtils
import com.newhome.app.dao.firebase.FirebaseAnimalProvider
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.BeforeClass

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAnimalProviderTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var transaction: Transaction
    private lateinit var nonDefaultBitmap: Bitmap

    private lateinit var imageProvider: IImageProvider

    private lateinit var provider: FirebaseAnimalProvider

    @Before
    fun setup() {
        val (f, t) = MockUtils.mockFirestore()
        firestore = f
        transaction = t
        nonDefaultBitmap = MockUtils.nonDefaultBitmap

        imageProvider = MockUtils.mockImageProvider("usuarios/userid")

        provider = FirebaseAnimalProvider(firestore)
    }

//    @Test
//    fun `verify get user`() = runTest {
//        val user = provider.getUser("userid").await()
//        val doc = firestore.collection("usuarios").document("userid")
//        coVerify(exactly = 1) { doc.get() }
//        assertEquals("userid", user.id)
//        assertEquals("username", user.nome)
//        assertEquals("details", user.detalhes)
//    }
//
//    @Test
//    fun `verify get nonexistent user`() = runTest {
//        val e = TestUtils.assertThrowsAsync<Exception> {
//            provider.getUser("nonexistentid").await()
//        }
//        assertEquals("User does not exist.", e.message)
//    }

    @Test
    fun `verify get imagem animal invalid id`() = runTest {
    }

    @Test
    fun `verify get imagem animal`() = runTest {
    }

    @Test
    fun `verify get todos animais`() = runTest {
    }

    @Test
    fun `verify get todos animais empty`() = runTest {
    }

    @Test
    fun `verify get animais postos adocao invalid id`() = runTest {
    }

    @Test
    fun `verify get animais postos adocao with invalid animal id on list`() = runTest {
    }

    @Test
    fun `verify get animais postos adocao empty`() = runTest {
    }

    @Test
    fun `verify get animais postos adocao`() = runTest {
    }

    @Test
    fun `verify get animais adotados invalid id`() = runTest {
    }

    @Test
    fun `verify get animais adotados with invalid animal id on list`() = runTest {
    }

    @Test
    fun `verify get animais adotados empty`() = runTest {
    }

    @Test
    fun `verify get animais adotados`() = runTest {
    }

    @Test
    fun `verify get animais solicitados invalid id`() = runTest {
    }

    @Test
    fun `verify get animais solicitados with invalid animal id on list`() = runTest {
    }

    @Test
    fun `verify get animais solicitados empty`() = runTest {
    }

    @Test
    fun `verify get animais solicitados`() = runTest {
    }
}
