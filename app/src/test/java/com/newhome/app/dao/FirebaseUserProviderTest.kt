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

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseUserProviderTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    lateinit var transaction: Transaction

    private lateinit var nonDefaultBitmap: Bitmap

    private lateinit var firestore: FirebaseFirestore
    private lateinit var imageProvider: IImageProvider

    private lateinit var provider: FirebaseUsuarioProvider

    @Before
    fun setup() {
        transaction = MockUtils.transaction

        nonDefaultBitmap = MockUtils.nonDefaultBitmap

        firestore = MockUtils.mockFirestore()
        imageProvider = MockUtils.mockImageProvider("usuarios/userid")

        provider = FirebaseUsuarioProvider(firestore, imageProvider)
    }

    @Test
    fun `verify get user`() = runTest {
        val user = provider.getUser("userid").await()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { doc.get() }
        assertEquals("userid", user.id)
        assertEquals("username", user.name)
        assertEquals("details", user.details)
    }

    @Test
    fun `verify get nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<Exception> {
            provider.getUser("nonexistentid").await()
        }
        assertEquals("User does not exist.", e.message)
    }

    @Test
    fun `verify create user`() = runTest {
        val user = NewUser(
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
        val user = UserData(
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
        val user = UserData(
            "nonexistentid",
            "username",
            "details"
        )
        val e = TestUtils.assertThrowsAsync<Exception> { provider.updateUser(user).await() }
        assertEquals("User does not exist.", e.message)
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
    @Suppress("DeferredResultUnused")
    fun `verify get user image`() = runTest {
        val image = provider.getUserImage("userid").await()
        coVerify(exactly = 1) { imageProvider.getImageOrDefault("usuarios/userid") }
        assertEquals(nonDefaultBitmap, image)
    }

    @Test
    fun `verify get nonexistent user image`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.getUserImage("nonexistentid").await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify set user image`() = runTest {
        provider.setUserImage("userid", nonDefaultBitmap).await()
        coVerify(exactly = 1) { imageProvider.saveImage("usuarios/userid", nonDefaultBitmap) }
    }

    @Test
    fun `verify set nonexistent user image`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.setUserImage("nonexistentid", nonDefaultBitmap).await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    fun `verify add animal to list placed for adoption nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.addAnimalIdToList("nonexistentid", "animalId", AnimalList.placedForAdoption)
                .await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    fun `verify add animal to list placed for adoption nonexistent list`() = runTest {
        // set lists to nonexistent
        val doc = firestore.collection("usuarios").document("userid")
        val data = MockUtils.transaction.get(doc)
        every { data.getData() } returns hashMapOf<String, Any>()

        provider.addAnimalIdToList("userid", "novoanimalid", AnimalList.placedForAdoption)
            .await()

        val listStr = AnimalList.placedForAdoption.toString()
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<List<String>> {
                assertTrue("Wrong id added", it.contains("novoanimalid"))
                assertEquals(1, it.size)
            })
        }
    }

    @Test
    fun `verify add animal to list placed for adoption`() = runTest {
        provider.addAnimalIdToList("userid", "novoanimalid", AnimalList.placedForAdoption)
            .await()

        val listStr = AnimalList.placedForAdoption.toString()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<List<String>> {
                assertTrue("Old id cannot be removed", it.contains("animalid"))
                assertTrue("Wrong id added", it.contains("novoanimalid"))
                assertEquals(2, it.size)
            })
        }
    }

    @Test
    fun `verify add animal to list adopted nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.addAnimalIdToList("nonexistentid", "animalId", AnimalList.adopted)
                .await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    fun `verify add animal to list adopted nonexistent list`() = runTest {
        // set lists to nonexistent
        val doc = firestore.collection("usuarios").document("userid")
        val data = MockUtils.transaction.get(doc)
        every { data.getData() } returns hashMapOf<String, Any>()

        provider.addAnimalIdToList("userid", "novoanimalid", AnimalList.adopted)
            .await()

        val listStr = AnimalList.adopted.toString()
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue("Wrong id added", it.contains("novoanimalid"))
                assertEquals(1, it.size)
            })
        }
    }

    @Test
    fun `verify add animal to list adopted`() = runTest {
        provider.addAnimalIdToList("userid", "novoanimalid", AnimalList.adopted)
            .await()

        val listStr = AnimalList.adopted.toString()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue("Old id cannot be removed", it.contains("animalid"))
                assertTrue("Wrong id added", it.contains("novoanimalid"))
                assertEquals(2, it.size)
            })
        }
    }

    @Test
    fun `verify add animal to list adoption requested nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.addAnimalIdToList("nonexistentid", "animalId", AnimalList.adoptionRequested)
                .await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    fun `verify add animal to list adoption requested nonexistent list`() = runTest {
        // set lists to nonexistent
        val doc = firestore.collection("usuarios").document("userid")
        val data = MockUtils.transaction.get(doc)
        every { data.getData() } returns hashMapOf<String, Any>()

        provider.addAnimalIdToList("userid", "novoanimalid", AnimalList.adoptionRequested)
            .await()

        val listStr = AnimalList.adoptionRequested.toString()
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue("Wrong id added", it.contains("novoanimalid"))
                assertEquals(1, it.size)
            })
        }
    }

    @Test
    fun `verify add animal to list adoption requested`() = runTest {
        provider.addAnimalIdToList("userid", "novoanimalid", AnimalList.adoptionRequested)
            .await()

        val listStr = AnimalList.adoptionRequested.toString()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue("Old id cannot be removed", it.contains("animalid"))
                assertTrue("Wrong id added", it.contains("novoanimalid"))
                assertEquals(2, it.size)
            })
        }
    }

    @Test
    fun `verify remove animal from list placed for adoption nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.removeAnimalIdFromList(
                "nonexistentid",
                "animalid",
                AnimalList.placedForAdoption
            ).await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    fun `verify remove animal from list placed for adoption to empty list`() = runTest {
        provider.removeAnimalIdFromList("userid", "animalid", AnimalList.placedForAdoption)
            .await()

        val listStr = AnimalList.placedForAdoption.toString()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<List<String>> {
                assertTrue(it.isEmpty())
            })
        }
    }

    @Test
    fun `verify remove animal from list placed for adoption`() = runTest {
        // set lists to nonexistent
        val doc = firestore.collection("usuarios").document("userid")
        val data = MockUtils.transaction.get(doc)
        every { data.getData() } returns hashMapOf<String, Any>(
            AnimalList.placedForAdoption.toString() to arrayListOf("animalid", "animalid2"),
        )

        provider.removeAnimalIdFromList("userid", "animalid", AnimalList.placedForAdoption)
            .await()

        val listStr = AnimalList.placedForAdoption.toString()
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<List<String>> {
                assertTrue("Wrong id removed", it.contains("animalid2"))
                assertEquals(1, it.size)
            })
        }
    }

    @Test
    fun `verify remove animal from list adopted nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.removeAnimalIdFromList(
                "nonexistentid",
                "animalid",
                AnimalList.adopted
            ).await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    fun `verify remove animal from list adopted to empty list`() = runTest {
        provider.removeAnimalIdFromList("userid", "animalid", AnimalList.adopted)
            .await()

        val listStr = AnimalList.adopted.toString()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue(it.isEmpty())
            })
        }
    }

    @Test
    fun `verify remove animal from list adopted`() = runTest {
        // set lists to nonexistent
        val doc = firestore.collection("usuarios").document("userid")
        val data = MockUtils.transaction.get(doc)
        every { data.getData() } returns hashMapOf<String, Any>(
            AnimalList.adopted.toString() to arrayListOf("animalid", "animalid2"),
        )

        provider.removeAnimalIdFromList("userid", "animalid", AnimalList.adopted)
            .await()

        val listStr = AnimalList.adopted.toString()
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue("Wrong id removed", it.contains("animalid2"))
                assertEquals(1, it.size)
            })
        }
    }

    @Test
    fun `verify remove animal from list adoption requested nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.removeAnimalIdFromList(
                "nonexistentid",
                "animalid",
                AnimalList.adoptionRequested
            ).await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    fun `verify remove animal from list adoption requested to empty list`() = runTest {
        provider.removeAnimalIdFromList("userid", "animalid", AnimalList.adoptionRequested)
            .await()

        val listStr = AnimalList.adoptionRequested.toString()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue(it.isEmpty())
            })
        }
    }

    @Test
    fun `verify remove animal from list adoption requested`() = runTest {
        // set lists to nonexistent
        val doc = firestore.collection("usuarios").document("userid")
        val data = MockUtils.transaction.get(doc)
        every { data.getData() } returns hashMapOf<String, Any>(
            AnimalList.adoptionRequested.toString() to arrayListOf("animalid", "animalid2"),
        )

        provider.removeAnimalIdFromList("userid", "animalid", AnimalList.adoptionRequested)
            .await()

        val listStr = AnimalList.adoptionRequested.toString()
        coVerify(exactly = 1) {
            transaction.update(doc, listStr, withArg<ArrayList<String>> {
                assertTrue("Wrong id removed", it.contains("animalid2"))
                assertEquals(1, it.size)
            })
        }
    }
}
