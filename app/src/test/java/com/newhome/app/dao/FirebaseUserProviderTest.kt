package com.newhome.app.dao

import android.graphics.Bitmap
import com.google.android.gms.tasks.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import com.newhome.app.MockUtils
import com.newhome.app.TestUtils
import com.newhome.app.dao.firebase.FirebaseUsuarioProvider
import com.newhome.app.dto.NewUser
import com.newhome.app.dto.UserData
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Ignore

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseUserProviderTest {
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

    private lateinit var provider: FirebaseUsuarioProvider

    @Before
    fun setup() {
        val (f, t) = MockUtils.mockFirestore()
        firestore = f
        transaction = t
        nonDefaultBitmap = MockUtils.nonDefaultBitmap

        imageProvider = MockUtils.mockImageProvider("usuarios/userid")

        provider = FirebaseUsuarioProvider(firestore)
    }

    @Test
    fun `verify get user`() = runTest {
        val user = provider.getUser(transaction, "userid")
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { transaction.get(doc) }
        val snap = transaction.get(doc)
        coVerify(exactly = 1) { snap.exists() }
        assertNotNull(user)
        assertEquals("userid", user!!.id)
        assertEquals("username", user.name)
        assertEquals("details", user.details)
    }

    @Test
    fun `verify get nonexistent user`() = runTest {
        val user = provider.getUser(transaction, "nonexistentid")
        val doc = firestore.collection("usuarios").document("nonexistentid")
        coVerify(exactly = 1) { transaction.get(doc) }
        val snap = transaction.get(doc)
        coVerify(exactly = 1) { snap.exists() }
        assertNull(user)
    }

    @Test
    fun `verify create user`() = runTest {
        val user = NewUser(
            "userid",
            "username",
            "details",
            18
        )
        provider.createUser(transaction, user)
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { transaction.set(doc, any()) }
    }

    @Test
    fun `verify update user`() = runTest {
        val user = UserData(
            "userid",
            "username",
            "details"
        )
        provider.updateUser(transaction, user)
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { transaction.update(doc, any()) }
    }

    @Test
    fun `verify update nonexistent user`() = runTest {
        val user = UserData(
            "nonexistentid",
            "username",
            "details"
        )
        TestUtils.assertThrowsAsync<Exception> {
            provider.updateUser(transaction, user)
        }
    }

    @Test
    @Ignore
    fun `verify delete user`() = runTest {
        // TODO implement
    }

    @Test
    @Ignore
    fun `verify delete nonexistent user`() = runTest {
        // TODO implement
    }

    @Test
    fun `verify get animal list nonexistent user`() = runTest {
        var list = provider.getAnimalList(
            transaction,
            "nonexistentid",
            AnimalList.placedForAdoption
        )
        assertNull(list)

        list = provider.getAnimalList(
            transaction,
            "nonexistentid",
            AnimalList.adopted
        )
        assertNull(list)

        list = provider.getAnimalList(
            transaction,
            "nonexistentid",
            AnimalList.adoptionRequested
        )
        assertNull(list)

        val doc = firestore.collection("usuarios").document("nonexistentid")
        coVerify(exactly = 3) { transaction.get(doc) }
    }

    @Test
    fun `verify get animal list nonexistent list`() = runTest {
        // set lists to nonexistent
        val doc = firestore.collection("usuarios").document("userid")
        val data = transaction.get(doc)
        every { data.data } returns hashMapOf<String, Any>()

        var list = provider.getAnimalList(
            transaction,
            "userid",
            AnimalList.placedForAdoption
        )
        assertNotNull(list)
        assertTrue(list!!.isEmpty())

        list = provider.getAnimalList(
            transaction,
            "userid",
            AnimalList.adopted
        )
        assertNotNull(list)
        assertTrue(list!!.isEmpty())

        list = provider.getAnimalList(
            transaction,
            "userid",
            AnimalList.adoptionRequested
        )
        assertNotNull(list)
        assertTrue(list!!.isEmpty())

        coVerify(exactly = 4) { transaction.get(doc) }
    }

    @Test
    fun `verify get animal list`() = runTest {
        var list = provider.getAnimalList(
            transaction,
            "userid",
            AnimalList.placedForAdoption
        )
        assertNotNull(list)
        assertTrue(list!!.isNotEmpty())

        list = provider.getAnimalList(
            transaction,
            "userid",
            AnimalList.adopted
        )
        assertNotNull(list)
        assertTrue(list!!.isNotEmpty())

        list = provider.getAnimalList(
            transaction,
            "userid",
            AnimalList.adoptionRequested
        )
        assertNotNull(list)
        assertTrue(list!!.isNotEmpty())

        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 3) { transaction.get(doc) }
    }

    @Test
    fun `verify set animal list nonexistent user`() = runTest {
        val list = arrayListOf("animalid")

        TestUtils.assertThrowsAsync<Exception> {
            provider.setAnimalList(
                transaction,
                "nonexistentid",
                AnimalList.placedForAdoption,
                list
            )
        }

        TestUtils.assertThrowsAsync<Exception> {
            provider.setAnimalList(
                transaction,
                "nonexistentid",
                AnimalList.adopted,
                list
            )
        }

        TestUtils.assertThrowsAsync<Exception> {
            provider.setAnimalList(
                transaction,
                "nonexistentid",
                AnimalList.adoptionRequested,
                list
            )
        }
    }

    @Test
    fun `verify set animal list`() = runTest {
        val list = arrayListOf("animalid")

        provider.setAnimalList(
            transaction,
            "userid",
            AnimalList.placedForAdoption,
            list
        )

        provider.setAnimalList(
            transaction,
            "userid",
            AnimalList.adopted,
            list
        )

        provider.setAnimalList(
            transaction,
            "userid",
            AnimalList.adoptionRequested,
            list
        )

        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { transaction.update(doc, AnimalList.placedForAdoption.toString(), list) }
        coVerify(exactly = 1) { transaction.update(doc, AnimalList.adopted.toString(), list) }
        coVerify(exactly = 1) { transaction.update(doc, AnimalList.adoptionRequested.toString(), list) }
    }
}
