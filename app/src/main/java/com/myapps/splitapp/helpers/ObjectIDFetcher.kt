package com.myapps.splitapp.helpers

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myapps.splitapp.models.UserAccount


fun fetchObjectsByIds(ids: List<String>, onResult: (Map<String, UserAccount>) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val reference = database.getReference("users")

    val results = mutableMapOf<String, UserAccount>()
    var remaining = ids.size

    ids.forEach { id ->
        reference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(UserAccount::class.java)
                    if (data != null) {
                        results[id] = data
                    }
                }
                remaining--
                if (remaining == 0) {
                    // All data fetched
                    onResult(results)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                remaining--
                if (remaining == 0) {
                    onResult(results)
                }
            }
        })
    }
}