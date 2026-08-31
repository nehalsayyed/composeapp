package org.example.project

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactItem(
    val id: Long,
    val name: String,
    val number: String,
    val photoUri: Uri?,
    val isFavorite: Boolean
)

data class CallLogItem(
    val id: Long,
    val name: String?,
    val number: String,
    val type: Int, // INCOMING_TYPE, OUTGOING_TYPE, MISSED_TYPE
    val timestamp: Long,
    val duration: Long
)

class TelephonyRepository(private val context: Context) {

    @SuppressLint("Range")
    suspend fun getContacts(): List<ContactItem> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ContactItem>()
        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.STARRED,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Unknown"
                val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                val starred = it.getInt(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)) == 1
                val photoUriStr = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                val photoUri = photoUriStr?.let { uri -> Uri.parse(uri) }

                contacts.add(ContactItem(id, name, number, photoUri, starred))
            }
        }
        contacts
    }

    @SuppressLint("Range")
    suspend fun getCallLogs(): List<CallLogItem> = withContext(Dispatchers.IO) {
        val callLogs = mutableListOf<CallLogItem>()
        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            ),
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(CallLog.Calls._ID))
                val name = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val number = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER)) ?: "Unknown"
                val type = it.getInt(it.getColumnIndex(CallLog.Calls.TYPE))
                val date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE))
                val duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION))

                callLogs.add(CallLogItem(id, name, number, type, date, duration))
            }
        }
        callLogs
    }
}
