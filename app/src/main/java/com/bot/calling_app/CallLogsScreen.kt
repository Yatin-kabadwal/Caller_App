package com.bot.calling_app

import android.content.Context
import android.provider.CallLog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.util.*

data class CallLogEntry(val number: String, val type: String, val date: String)

@Composable
fun CallLogsScreen() {
    val context = LocalContext.current
    val callLogs = remember { mutableStateListOf<CallLogEntry>() }

    LaunchedEffect(Unit) {
        callLogs.addAll(getCallLogs(context))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Call Logs", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(callLogs) { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Number: ${log.number}")
                        Text(text = "Type: ${log.type}")
                        Text(text = "Date: ${log.date}")
                    }
                }
            }
        }
    }
}

fun getCallLogs(context: Context): List<CallLogEntry> {
    val logs = mutableListOf<CallLogEntry>()
    val cursor = context.contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
        null, null, CallLog.Calls.DATE + " DESC"
    )

    cursor?.use {
        val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
        val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
        val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)

        while (it.moveToNext()) {
            val number = it.getString(numberIndex)
            val type = when (it.getInt(typeIndex)) {
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                else -> "Unknown"
            }
            val date = Date(it.getLong(dateIndex)).toString()

            logs.add(CallLogEntry(number, type, date))
        }
    }
    return logs
}
