package com.bot.calling_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to represent a contact
data class Contact(val name: String, val phoneNumber: String)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val contacts = remember { mutableStateListOf<Contact>() }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        contacts.addAll(getContacts(context))
    }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery.text, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery.text)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Contacts",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            placeholder = { Text("Search contacts") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredContacts) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedContact = contact },
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Contact Icon",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = contact.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { makesCall(context, contact.phoneNumber) }) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                        }
                    }
                }
            }
        }

        selectedContact?.let { contact ->
            AlertDialog(
                onDismissRequest = { selectedContact = null },
                title = { Text(text = "Contact Options") },
                text = { Text(text = "What would you like to do with ${contact.name}?") },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { makesCall(context, contact.phoneNumber) }) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                        }
                        IconButton(onClick = { sendMessage(context, contact.phoneNumber) }) {
                            Icon(Icons.Default.Sms, contentDescription = "Message")
                        }
                        IconButton(onClick = {
                            contacts.remove(contact)
                            selectedContact = null
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedContact = null }) {
                        Text("Cancel")
                    }
                }
            )
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

fun makesCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
    context.startActivity(intent)
}

fun sendMessage(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
    context.startActivity(intent)
}
