package com.newhome.app.dao.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.newhome.app.dao.IStoreProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

open class StoreProvider(private val db: FirebaseFirestore): IStoreProvider {
    override suspend fun <T> runTransaction(func: (Transaction) -> T): Deferred<T> =
        CoroutineScope(Dispatchers.Main).async {
            return@async db.runTransaction(func).await()
        }
}
