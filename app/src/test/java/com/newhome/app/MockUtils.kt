package com.newhome.app

import android.graphics.Bitmap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.newhome.app.dao.FirebaseUsuarioProviderTest
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Credenciais
import com.newhome.app.dto.UsuarioData
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MockUtils {
    companion object {
        lateinit var defaultBitmap: Bitmap

        lateinit var getImageTask: Deferred<Bitmap>
        lateinit var emptyTask: Deferred<Unit>
        lateinit var exceptionTask: Deferred<Nothing>

        fun init() {
            Dispatchers.setMain(StandardTestDispatcher())

            defaultBitmap = mockk()

            getImageTask = CoroutineScope(Dispatchers.Main).async { defaultBitmap }
            emptyTask = CoroutineScope(Dispatchers.Main).async { }
            exceptionTask = CoroutineScope(Dispatchers.Main).async { throw Exception() }
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

        fun mockImageProvider(imagePath: String): IImageProvider {
            val provider = mockk<IImageProvider>()

            val getImageTask = CoroutineScope(Dispatchers.Main).async { return@async defaultBitmap }

            val pathCapture = slot<String>()
            coEvery { provider.saveImage(capture(pathCapture), any()) } answers {
                if (pathCapture.captured == imagePath) emptyTask
                else exceptionTask
            }
            coEvery { provider.removeImage(capture(pathCapture)) } answers {
                if (pathCapture.captured == imagePath) emptyTask
                else exceptionTask
            }
            coEvery { provider.getImageOrDefault(capture(pathCapture)) } answers {
                if (pathCapture.captured == imagePath) getImageTask
                else exceptionTask
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
                    credenciaisCapture.captured.email == "emailvalido@email.com" &&
                    credenciaisCapture.captured.senha == "#SenhaValida1"
                ) emptyTask
                else exceptionTask
            }
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
                else exceptionTask
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