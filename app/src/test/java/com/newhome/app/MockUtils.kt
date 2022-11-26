package com.newhome.app

import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Credenciais
import com.newhome.app.dto.UsuarioData
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MockUtils {
    companion object {
        lateinit var defaultBitmap: Bitmap; private set
        lateinit var nonDefaultBitmap: Bitmap; private set

        lateinit var getDefaultImageTask: Deferred<Bitmap>; private set
        lateinit var getImageTask: Deferred<Bitmap>; private set
        lateinit var emptyTask: Deferred<Unit>; private set
        lateinit var exceptionTask: Deferred<Nothing>; private set

        fun init() {
            Dispatchers.setMain(StandardTestDispatcher())

            defaultBitmap = mockk()
            nonDefaultBitmap = mockk()

            getDefaultImageTask = CoroutineScope(Dispatchers.Main).async { defaultBitmap }
            getImageTask = CoroutineScope(Dispatchers.Main).async { nonDefaultBitmap }
            emptyTask = CoroutineScope(Dispatchers.Main).async { }
            exceptionTask = CoroutineScope(Dispatchers.Main).async { throw Exception() }
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

            val credenciaisCapture = slot<Credenciais>()
            coEvery { provider.getContaID() } returns "currentuserid"
            coEvery { provider.enviarEmailConfirmacao() } returns emptyTask
            coEvery { provider.emailConfirmacaoVerificado() } returns true
            coEvery { provider.criarConta(any()) } returns emptyTask
            coEvery { provider.logar(capture(credenciaisCapture)) } answers {
                if (
                    credenciaisCapture.captured.email == "emailcorreto@example.com" &&
                    credenciaisCapture.captured.senha == "#SenhaCorreta123"
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

            val currentUser = UsuarioData("currentuserid", "username", "details")
            val user = UsuarioData("userid", "username", "details")

            val getCurrentUserTask = CoroutineScope(Dispatchers.Main).async { currentUser }
            val getUserTask = CoroutineScope(Dispatchers.Main).async { user }

            val userId = slot<String>()
            val userCapture = slot<UsuarioData>()
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