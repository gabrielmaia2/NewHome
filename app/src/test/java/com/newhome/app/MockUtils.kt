package com.newhome.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.newhome.app.utils.Utils
import com.newhome.app.dao.AnimalList
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Credentials
import com.newhome.app.dto.UserData
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.nio.charset.Charset

@OptIn(ExperimentalCoroutinesApi::class)
class MockUtils {
    companion object {
        lateinit var applicationContext: Context; private set

        lateinit var defaultBitmap: Bitmap; private set
        lateinit var nonDefaultBitmap: Bitmap; private set

        lateinit var getDefaultImageTask: Deferred<Bitmap>; private set
        lateinit var getImageTask: Deferred<Bitmap>; private set
        lateinit var emptyTask: Deferred<Unit>; private set
        lateinit var exceptionTask: Deferred<Nothing>; private set

        fun init() {
            Dispatchers.setMain(StandardTestDispatcher())

            applicationContext = mockk()

            defaultBitmap = mockk()
            nonDefaultBitmap = mockk()

            getDefaultImageTask = CoroutineScope(Dispatchers.Main).async { defaultBitmap }
            getImageTask = CoroutineScope(Dispatchers.Main).async { nonDefaultBitmap }
            emptyTask = CoroutineScope(Dispatchers.Main).async { }
            exceptionTask = CoroutineScope(Dispatchers.Main).async { throw Exception() }
        }

        // mocks storage, defaultbitmap, nonDefaultBitmap and byteArray
        fun mockFirebaseStorage(vararg imagePath: String): Pair<FirebaseStorage, ByteArray> {
            val storage = mockk<FirebaseStorage>()
            val byteArray = "awdkfosigmeuslzo".toByteArray(Charset.forName("ascii"))

            val storageRef = mockk<StorageReference>()

            every { storage.reference } returns storageRef

            mockkStatic(TextUtils::class)
            every { TextUtils.isEmpty(any()) } answers { arg<String>(0).isEmpty() }

            mockkObject(Utils)
            val btCapture = slot<ByteArray>()
            every { Utils.sha256(capture(btCapture)) } answers {
                @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
                if (btCapture.captured == byteArray) "shaByteArray"
                else "defaultsha"
            }
            val imgCapture = slot<Bitmap>()
            every { Utils.bitmapToJPEGByteArray(capture(imgCapture), any()) } answers {
                if (imgCapture.captured == nonDefaultBitmap) byteArray
                else "awdkfosigmeufslz".toByteArray(Charset.forName("ascii"))
            }

            val pathRef = mockk<StorageReference>()
            val anyRef = mockk<StorageReference>()

            mockkStatic(BitmapFactory::class)
            val baCapture = slot<ByteArray>()
            every { BitmapFactory.decodeByteArray(capture(baCapture), any(), any()) } answers {
                @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
                if (baCapture.captured == byteArray) nonDefaultBitmap
                else defaultBitmap
            }
            every {
                BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.image_default
                )
            } returns defaultBitmap

            val pathCapture = slot<String>()
            every { storageRef.child(capture(pathCapture)) } answers {
                if (imagePath.contains(pathCapture.captured.dropLast(4))) pathRef
                else anyRef
            }

            val md1 = mockk<StorageMetadata>()
            every { md1.getCustomMetadata("sha256sum") } returns "shaByteArray"
            val md2 = mockk<StorageMetadata>()
            every { md2.getCustomMetadata("sha256sum") } returns null

            val storageException = mockk<StorageException>()
            every { storageException.errorCode } returns StorageException.ERROR_OBJECT_NOT_FOUND
            every { storageException.cause } returns null

            coEvery { pathRef.putBytes(any(), any()) } returns TestUtils.createSuccessTask(mockk())
            coEvery { pathRef.getBytes(any()) } returns TestUtils.createSuccessTask(byteArray)
            coEvery { pathRef.delete() } returns TestUtils.createSuccessTask(mockk())
            coEvery { pathRef.metadata } returns TestUtils.createSuccessTask(md1)

            coEvery { anyRef.putBytes(any(), any()) } returns TestUtils.createSuccessTask(mockk())
            coEvery { anyRef.getBytes(any()) } returns TestUtils.createFailureTask(storageException)
            coEvery { anyRef.delete() } returns TestUtils.createFailureTask(storageException)
            coEvery { anyRef.metadata } returns TestUtils.createSuccessTask(md2)

            return Pair(storage, byteArray)
        }

        fun mockAuthUI(): AuthUI {
            val authUI = mockk<AuthUI>()

            coEvery { authUI.signOut(any()) } returns TestUtils.createVoidSuccessTask()
            coEvery { authUI.delete(any()) } returns TestUtils.createVoidSuccessTask()

            return authUI
        }

        fun mockFirebaseAuth(): FirebaseAuth {
            val auth = mockk<FirebaseAuth>()

            val credential = mockk<AuthCredential>()
            mockkStatic(GoogleAuthProvider::getCredential)
            every { GoogleAuthProvider.getCredential(any(), any()) } returns credential

            val currentUser = mockk<FirebaseUser>()
            coEvery { currentUser.uid } returns "currentuserid"
            coEvery { currentUser.sendEmailVerification() } returns TestUtils.createVoidSuccessTask()
            coEvery { currentUser.isEmailVerified } returns true

            val emailCapture = slot<String>()
            val senhaCapture = slot<String>()
            coEvery { auth.currentUser } returns currentUser
            coEvery {
                auth.createUserWithEmailAndPassword(any(), any())
            } returns TestUtils.createSuccessTask(mockk())
            coEvery {
                auth.signInWithEmailAndPassword(capture(emailCapture), capture(senhaCapture))
            } answers {
                if (
                    emailCapture.captured == "emailcorreto@example.com" &&
                    senhaCapture.captured == "#SenhaCorreta123"
                ) TestUtils.createSuccessTask(mockk())
                else TestUtils.createFailureTask(Exception())
            }
            coEvery {
                auth.signInWithCredential(any())
            } returns TestUtils.createSuccessTask(mockk())

            return auth
        }

        // mocks firestore and transaction
        fun mockFirestore(): Pair<FirebaseFirestore, Transaction> {
            val firestore = mockk<FirebaseFirestore>()
            val transaction = mockk<Transaction>()

            val setOptions = mockk<SetOptions>()
            mockkStatic(SetOptions::merge)
            every { SetOptions.merge() } returns setOptions

            val userCollection = mockk<CollectionReference>()
            val animalCollection = mockk<CollectionReference>()

            val collectionId = slot<String>()
            coEvery { firestore.collection(capture(collectionId)) } answers {
                if (collectionId.captured == "usuarios") userCollection
                else if (collectionId.captured == "animais") animalCollection
                else throw Exception("Invalid collection")
            }

            val userDoc = mockk<DocumentReference>()
            val anyUserDoc = mockk<DocumentReference>()
            val animalDoc = mockk<DocumentReference>()
            val anyAnimalDoc = mockk<DocumentReference>()

            val userId = slot<String>()
            coEvery { userCollection.document(capture(userId)) } answers {
                if (userId.captured == "userid") userDoc
                else anyUserDoc
            }
            coEvery { animalCollection.document(capture(userId)) } answers {
                if (userId.captured == "animalid") animalDoc
                else anyAnimalDoc
            }

            val func = slot<Transaction.Function<Unit>>()
            coEvery { firestore.runTransaction(capture(func)) } answers {
                TestUtils.createSuccessTask(func.captured.apply(transaction))
            }

            val userSnap = mockk<DocumentSnapshot>()
            val anyUserSnap = mockk<DocumentSnapshot>()
            val animalSnap = mockk<DocumentSnapshot>()
            val anyAnimalSnap = mockk<DocumentSnapshot>()
            every { userSnap.id } returns "userid"
            every { userSnap.data } returns hashMapOf<String, Any>(
                "nome" to "username",
                "detalhes" to "details",
                AnimalList.placedForAdoption.toString() to arrayListOf("animalid"),
                AnimalList.adopted.toString() to arrayListOf("animalid"),
                AnimalList.adoptionRequested.toString() to arrayListOf("animalid")
            )
            every { userSnap.exists() } returns true
            every { anyUserSnap.id } returns "nonexistentid"
            every { anyUserSnap.data } returns null
            every { anyUserSnap.exists() } returns false
            every { animalSnap.id } returns "animalid"
            every { animalSnap.data } returns hashMapOf<String, Any>(
                "nome" to "animal name",
                "dono" to "userid",
                "adotador" to "",
                "detalhes" to "details",
                "detalhesAdocao" to "",
                "buscando" to false,
                "solicitadores" to ArrayList<String>()
            )
            every { animalSnap.exists() } returns true
            every { anyAnimalSnap.id } returns "nonexistentid"
            every { anyAnimalSnap.data } returns null
            every { anyAnimalSnap.exists() } returns false

            val docSlot = slot<DocumentReference>()
            every { transaction.get(capture(docSlot)) } answers {
                if (docSlot.captured == userDoc) userSnap
                else if (docSlot.captured == anyUserDoc) anyUserSnap
                else if (docSlot.captured == animalDoc) animalSnap
                else if (docSlot.captured == anyAnimalDoc) anyAnimalSnap
                else throw Exception("Invalid document")
            }
            every { transaction.set(capture(docSlot), any()) } answers {
                if (docSlot.captured == userDoc) transaction
                else if (docSlot.captured == anyUserDoc) transaction
                else if (docSlot.captured == animalDoc) transaction
                else if (docSlot.captured == anyAnimalDoc) transaction
                else throw Exception("Invalid document")
            }
            every {
                transaction.set(any(), any(), SetOptions.merge())
            } throws Exception("Use update() instead of set() with merge")
            every { transaction.update(capture(docSlot), any()) } answers {
                if (docSlot.captured == userDoc) transaction
                else if (docSlot.captured == animalDoc) transaction
                else throw Exception("Invalid document")
            }
            every { transaction.update(capture(docSlot), any<String>(), any()) } answers {
                if (docSlot.captured == userDoc) transaction
                else if (docSlot.captured == animalDoc) transaction
                else throw Exception("Invalid document")
            }

            return Pair(firestore, transaction)
        }

        /**
         * Mocks image provider, asserting when saving or removing that
         * the path is one of the paths provided when mocking
         */
        fun mockImageProvider(vararg imagePath: String): IImageProvider {
            val provider = mockk<IImageProvider>()

            val pathCapture = slot<String>()
            coEvery { provider.saveImage(capture(pathCapture), any()) } answers {
                CoroutineScope(Dispatchers.Main).async {
                    assertTrue(
                        "Wrong path provided",
                        imagePath.contains(pathCapture.captured)
                    )
                }
            }
            coEvery { provider.removeImage(capture(pathCapture)) } answers {
                CoroutineScope(Dispatchers.Main).async {
                    assertTrue(
                        "Wrong path provided",
                        imagePath.contains(pathCapture.captured)
                    )
                }
            }
            coEvery { provider.getImageOrDefault(capture(pathCapture)) } answers {
                if (imagePath.contains(pathCapture.captured)) getImageTask
                else getDefaultImageTask
            }

            coEvery { provider.getAnimalImage(capture(pathCapture)) } answers {
                val paths = imagePath
                    .filter { p -> p.startsWith("animais/") }
                    .map { p -> p.split("/")[1] }
                if (paths.contains(pathCapture.captured)) getImageTask
                else getDefaultImageTask
            }
            coEvery { provider.getUserImage(capture(pathCapture)) } answers {
                val paths = imagePath
                    .filter { p -> p.startsWith("usuarios/") }
                    .map { p -> p.split("/")[1] }
                if (paths.contains(pathCapture.captured)) getImageTask
                else getDefaultImageTask
            }
            coEvery { provider.saveAnimalImage(capture(pathCapture), any()) } answers {
                CoroutineScope(Dispatchers.Main).async {
                    val paths = imagePath
                        .filter { p -> p.startsWith("animais/") }
                        .map { p -> p.split("/")[1] }
                    assertTrue(
                        "Wrong path provided",
                        paths.contains(pathCapture.captured)
                    )
                }
            }
            coEvery { provider.saveUserImage(capture(pathCapture), any()) } answers {
                CoroutineScope(Dispatchers.Main).async {
                    val paths = imagePath
                        .filter { p -> p.startsWith("usuarios/") }
                        .map { p -> p.split("/")[1] }
                    assertTrue(
                        "Wrong path provided",
                        paths.contains(pathCapture.captured)
                    )
                }
            }

            return provider
        }

        fun mockContaProvider(): IContaProvider {
            val provider = mockk<IContaProvider>()

            val credentialsCapture = slot<Credentials>()
            every { provider.getContaID() } returns "currentuserid"
            coEvery { provider.enviarEmailConfirmacao() } returns emptyTask
            every { provider.emailConfirmacaoVerificado() } returns true
            coEvery { provider.criarConta(any()) } returns emptyTask
            coEvery { provider.logar(capture(credentialsCapture)) } coAnswers {
                if (
                    credentialsCapture.captured.email == "emailcorreto@example.com" &&
                    credentialsCapture.captured.password == "#SenhaCorreta123"
                ) emptyTask
                else exceptionTask
            }
            coEvery { provider.entrarComGoogle(any()) } returns emptyTask
            coEvery { provider.sair() } returns emptyTask
            coEvery { provider.excluirConta() } returns emptyTask

            return provider
        }

        fun mockUsuarioProvider(): IUsuarioProvider {
            val provider = mockk<IUsuarioProvider>()

            val currentUser = UserData("currentuserid", "username", "details")
            val user = UserData("userid", "username", "details")

            val funcSlot = slot<(Transaction) -> Any>()
            coEvery { provider.runTransaction(capture(funcSlot)) } coAnswers {
                CoroutineScope(Dispatchers.Main).async { funcSlot.captured.invoke(mockk()) }
            }

            val userId = slot<String>()
            val userCapture = slot<UserData>()
            every { provider.getUser(any(), capture(userId)) } answers {
                if (userId.captured == "currentuserid") currentUser
                else if (userId.captured == "userid") user
                else throw Exception()
            }
            every { provider.createUser(any(), any()) } returns Unit
            every { provider.updateUser(any(), capture(userCapture)) } answers {
                if (userCapture.captured.id == "currentuserid") Unit
                else if (userCapture.captured.id == "userid") Unit
                else throw Exception()
            }
            every { provider.deleteUser(any(), capture(userId)) } answers {
                if (userId.captured == "currentuserid") Unit
                else if (userId.captured == "userid") Unit
                else throw Exception()
            }

            val animalList = arrayListOf("animalid")

            every { provider.getAnimalList(any(), capture(userId), any()) } answers {
                if (userId.captured == "currentuserid") animalList
                else if (userId.captured == "userid") animalList
                else throw Exception()
            }
            every { provider.setAnimalList(any(), capture(userId), any(), any()) } answers {
                if (userId.captured == "currentuserid") Unit
                else if (userId.captured == "userid") Unit
                else throw Exception()
            }

            return provider
        }
    }
}
