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
import java.nio.charset.Charset

@OptIn(ExperimentalCoroutinesApi::class)
class MockUtils {
    companion object {
        lateinit var applicationContext: Context; private set
        lateinit var transaction: Transaction; private set

        lateinit var defaultBitmap: Bitmap; private set
        lateinit var nonDefaultBitmap: Bitmap; private set
        lateinit var byteArray: ByteArray

        lateinit var getDefaultImageTask: Deferred<Bitmap>; private set
        lateinit var getImageTask: Deferred<Bitmap>; private set
        lateinit var emptyTask: Deferred<Unit>; private set
        lateinit var exceptionTask: Deferred<Nothing>; private set

        fun init() {
            Dispatchers.setMain(StandardTestDispatcher())

            applicationContext = mockk()

            transaction = mockk()

            defaultBitmap = mockk()
            nonDefaultBitmap = mockk()
            byteArray = "awdkfosigmeuslzo".toByteArray(Charset.forName("ascii"))

            getDefaultImageTask = CoroutineScope(Dispatchers.Main).async { defaultBitmap }
            getImageTask = CoroutineScope(Dispatchers.Main).async { nonDefaultBitmap }
            emptyTask = CoroutineScope(Dispatchers.Main).async { }
            exceptionTask = CoroutineScope(Dispatchers.Main).async { throw Exception() }
        }

        fun mockFirebaseStorage(vararg imagePath: String): FirebaseStorage {
            val storage = mockk<FirebaseStorage>()
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

            return storage
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

        fun mockFirestore(): FirebaseFirestore {
            val firestore = mockk<FirebaseFirestore>()

            val setOptions = mockk<SetOptions>()
            mockkStatic(SetOptions::merge)
            every { SetOptions.merge() } returns setOptions

            val userSnap = mockk<DocumentSnapshot>()
            every { userSnap.id } returns "userid"
            every { userSnap.data } returns hashMapOf(
                "nome" to "username",
                "detalhes" to "details"
            ) as Map<String, Any>
            every { userSnap.exists() } returns true
            val anyUserSnap = mockk<DocumentSnapshot>()
            every { anyUserSnap.id } returns "nonexistentid"
            every { anyUserSnap.data } throws Exception()
            every { anyUserSnap.exists() } returns false

            val userCollection = mockk<CollectionReference>()

            val collectionId = slot<String>()
            coEvery { firestore.collection(capture(collectionId)) } answers {
                if (collectionId.captured == "usuarios") userCollection
                else throw Exception("Invalid collection")
            }

            val userDoc = mockk<DocumentReference>()
            val anyDoc = mockk<DocumentReference>()

            val userId = slot<String>()
            coEvery { userCollection.document(capture(userId)) } answers {
                if (userId.captured == "userid") userDoc
                else anyDoc
            }

            coEvery { userDoc.get() } returns TestUtils.createSuccessTask(userSnap)
            coEvery { anyDoc.get() } returns TestUtils.createSuccessTask(anyUserSnap)
            coEvery { userDoc.set(any()) } returns TestUtils.createVoidSuccessTask()
            coEvery {
                userDoc.set(
                    any(),
                    SetOptions.merge()
                )
            } returns TestUtils.createVoidSuccessTask()
            coEvery {
                anyDoc.set(
                    any(),
                    SetOptions.merge()
                )
            } returns TestUtils.createVoidSuccessTask()

            val func = slot<Transaction.Function<Unit>>()
            coEvery { firestore.runTransaction(capture(func)) } answers {
                TestUtils.createSuccessTask(func.captured.apply(transaction))
            }

            val userData = mockk<DocumentSnapshot>()
            val anyData = mockk<DocumentSnapshot>()
            every { userData.getData() } returns hashMapOf<String, Any>(
                AnimalList.placedForAdoption.toString() to arrayListOf("animalid"),
                AnimalList.adopted.toString() to arrayListOf("animalid"),
                AnimalList.adoptionRequested.toString() to arrayListOf("animalid")
            )
            every { userData.exists() } returns true
            every { anyData.exists() } returns false

            val captureRef = slot<DocumentReference>()
            every { transaction.get(capture(captureRef)) } answers {
                if (captureRef.captured == userDoc) userData
                else anyData
            }
            every { transaction.update(any(), any()) } returns transaction
            every { transaction.update(any(), any<String>(), any()) } returns transaction

            return firestore
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

            return provider
        }

        fun mockContaProvider(): IContaProvider {
            val provider = mockk<IContaProvider>()

            val credentialsCapture = slot<Credentials>()
            coEvery { provider.getContaID() } returns "currentuserid"
            coEvery { provider.enviarEmailConfirmacao() } returns emptyTask
            coEvery { provider.emailConfirmacaoVerificado() } returns true
            coEvery { provider.criarConta(any()) } returns emptyTask
            coEvery { provider.logar(capture(credentialsCapture)) } answers {
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

            val getCurrentUserTask = CoroutineScope(Dispatchers.Main).async { currentUser }
            val getUserTask = CoroutineScope(Dispatchers.Main).async { user }

            val userId = slot<String>()
            val userCapture = slot<UserData>()
            coEvery { provider.getUser(capture(userId)) } answers {
                if (userId.captured == "currentuserid") getCurrentUserTask
                else if (userId.captured == "userid") getUserTask
                else exceptionTask
            }
            coEvery { provider.createUser(any()) } returns emptyTask
            coEvery { provider.updateUser(capture(userCapture)) } answers {
                if (userCapture.captured.id == "currentuserid") emptyTask
                else if (userCapture.captured.id == "userid") emptyTask
                else exceptionTask
            }
            coEvery { provider.deleteUser(capture(userId)) } answers {
                if (userId.captured == "currentuserid") emptyTask
                else if (userId.captured == "userid") emptyTask
                else exceptionTask
            }
            coEvery { provider.getUserImage(capture(userId)) } answers {
                if (userId.captured == "currentuserid") getImageTask
                else if (userId.captured == "userid") getImageTask
                else getDefaultImageTask
            }
            coEvery { provider.setUserImage(capture(userId), any()) } answers {
                if (userId.captured == "currentuserid") emptyTask
                else if (userId.captured == "userid") emptyTask
                else exceptionTask
            }

            return provider
        }
    }
}