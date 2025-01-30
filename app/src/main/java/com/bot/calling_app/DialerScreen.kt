package com.bot.calling_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

@Composable
fun DialerScreen() {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Enter Phone Number") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { makeCall(context, phoneNumber) }) {
            Text("Call")
        }
    }
}

fun makeCall(context: Context, number: String) {
    if (number.isNotEmpty()) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show()
    }
}
