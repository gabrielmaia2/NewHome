package com.newhome.app.dao

import com.google.firebase.firestore.Transaction
import com.newhome.app.dto.NewUser
import kotlinx.coroutines.Deferred
import com.newhome.app.dto.UserData

interface IUsuarioProvider : IStoreProvider {
    // gets user or null if does not exist
    fun getUser(t: Transaction, id: String): UserData?

    fun createUser(t: Transaction, usuario: NewUser)

    fun updateUser(t: Transaction, usuario: UserData)

    fun deleteUser(t: Transaction, id: String)

    // gets animal list or null if does not exist
    fun getAnimalList(
        t: Transaction,
        id: String,
        list: AnimalList
    ): ArrayList<String>?

    fun setAnimalList(
        t: Transaction,
        id: String,
        list: AnimalList,
        data: List<String>
    )
}
