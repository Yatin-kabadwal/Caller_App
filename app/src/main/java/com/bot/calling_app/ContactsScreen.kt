package com.bot.calling_app

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

data class Contact(val name: String, val phoneNumber: String)

@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val contacts = remember { mutableStateListOf<Contact>() }

    LaunchedEffect(Unit) {
        contacts.addAll(getContacts(context))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Contacts", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(contacts) { contact ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Name: ${contact.name}")
                        Text(text = "Phone: ${contact.phoneNumber}")
                    }
                }
            }
        }
    }
}

fun getContacts(context: Context): List<Contact> {
    val contactsList = mutableListOf<Contact>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
        null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            val phoneNumber = it.getString(numberIndex)
            contactsList.add(Contact(name, phoneNumber))
        }
    }
    return contactsList
}
