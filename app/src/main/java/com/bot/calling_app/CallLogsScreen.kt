package com.bot.calling_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data Class for Call Log Entries
data class CallLogEntry(val number: String, val name: String?, val type: String, val date: String)

@Composable
fun CallLogsScreen() {
    val context = LocalContext.current
    val callLogs = remember { mutableStateListOf<CallLogEntry>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val logs = getCallLogs(context)
            callLogs.clear()
            callLogs.addAll(logs)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Call Logs",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (callLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(callLogs) { log ->
                    CallLogCard(log, context)
                }
            }
        }
    }
}

@Composable
fun CallLogCard(log: CallLogEntry, context: Context) {
    val typeColor = when (log.type) {
        "Incoming" -> Color(0xFF4CAF50)
        "Outgoing" -> Color(0xFF2196F3)
        "Missed" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(typeColor, shape = RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.name ?: log.number,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = log.type,
                    color = typeColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = log.date,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = { initiateCall(context, log.number) }) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun getCallLogs(context: Context): List<CallLogEntry> {
    val logs = mutableListOf<CallLogEntry>()
    val resolver = context.contentResolver
    val cursor = resolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
        null, null, "${CallLog.Calls.DATE} DESC LIMIT 50" // Fetch only the latest 50 logs for speed
    )

    cursor?.use {
        val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
        val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
        val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)

        val contactCache = mutableMapOf<String, String?>() // Cache for faster lookups

        while (it.moveToNext()) {
            val number = if (numberIndex != -1) it.getString(numberIndex) else "Unknown Number"

            // Check cache for contact name
            val name = contactCache.getOrPut(number) { getContactName(context, number) }

            val type = when (if (typeIndex != -1) it.getInt(typeIndex) else -1) {
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                else -> "Unknown"
            }

            val date = if (dateIndex != -1)
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it.getLong(dateIndex)))
            else
                "Unknown Date"

            logs.add(CallLogEntry(number, name, type, date))
        }
    }
    return logs
}

fun getContactName(context: Context, phoneNumber: String): String? {
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
    val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
            if (nameIndex != -1) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return null
}

fun initiateCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$phoneNumber")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
