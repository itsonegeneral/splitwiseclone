package com.myapps.splitwiseclone.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

fun getContactNumbers(context: Context): List<String> {
    val contactNumbers = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val phoneNumber =
                    it.getString(numberIndex)
                        .replace("(", "")
                        .replace(")", "")
                        .replace(" ", "")
                        .replace("-", "")
                contactNumbers.add(phoneNumber)
            }
        }
    }
    return contactNumbers
}
