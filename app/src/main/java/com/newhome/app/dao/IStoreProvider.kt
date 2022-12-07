package com.newhome.app.dao

import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.Deferred

interface IStoreProvider {
    suspend fun <T> runTransaction(func: (Transaction) -> T): Deferred<T>
}