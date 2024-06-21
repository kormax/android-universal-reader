package com.kormax.universalreader


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kormax.universalreader.Constants.Companion.NFC_LOG_MESSAGE
import com.kormax.universalreader.Constants.Companion.VAS_READ_MESSAGE
import com.kormax.universalreader.apple.vas.VasResult
import com.kormax.universalreader.google.smarttap.SmartTapObjectCustomer
import com.kormax.universalreader.google.smarttap.SmartTapObjectPass
import com.kormax.universalreader.google.smarttap.SmartTapResult
import com.kormax.universalreader.iso7816.Iso7816Aid
import com.kormax.universalreader.iso7816.Iso7816Command
import com.kormax.universalreader.model.ReaderConfigurationModel
import com.kormax.universalreader.ui.theme.UniversalReaderTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private var vasDone = false
    private var hook = { _: String, _: Any -> }
    private var configuration = ValueAddedServicesReaderConfiguration(null, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        try {
            val configurationModelString: String =
                PreferenceManager.getDefaultSharedPreferences(this)!!.getString("configuration", "")
                    .toString()

            // Deserialize JSON string into MyDataModel object
            val model =
                configurationModelString.let { Json.decodeFromString<ReaderConfigurationModel>(it) }

            configuration = model.load()
        } catch (e: Exception) {
            Log.e("MainActivity", "${e}")
        }

        enableEdgeToEdge()
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            val configurationFileLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri?
                    ->
                    if (uri == null) {
                        return@rememberLauncherForActivityResult
                    }

                    try {
                        // Handle the selected file URI here
                        val jsonString =
                            loadJsonFile(context = this, uri)
                                ?: return@rememberLauncherForActivityResult

                        // Deserialize JSON string into MyDataModel object
                        val model =
                            jsonString.let { Json.decodeFromString<ReaderConfigurationModel>(it) }

                        configuration = model.load()
                        PreferenceManager.getDefaultSharedPreferences(this)
                            .edit()
                            .putString("configuration", jsonString)
                            .apply()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Configuration loaded for" +
                                    " vas=${if (configuration.vas != null) configuration.vas?.merchants?.size else 0}" +
                                    " gst=${configuration.smartTap != null}",
                            )
                        }
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Could not load file due to ${e}",
                            )
                        }
                    }
                }

            var modeMenuExpanded by remember { mutableStateOf(false) }
            var logsEnabled by remember { mutableStateOf(true) }
            var commandsEnabled by remember { mutableStateOf(true) }
            var responsesEnabled by remember { mutableStateOf(true) }

            hook = { type, value ->
                when (type) {
                    "log" -> {
                        Log.i("Hook", value as String)
                        if (logsEnabled) {
                            this.sendMessage(value)
                        }
                    }
                    "command" -> {
                        Log.i(type.uppercase(), value.toString())
                        if (commandsEnabled) {
                            this.sendMessage(value.toString())
                        }
                    }
                    "response" -> {
                        Log.i(type.uppercase(), value.toString())
                        if (responsesEnabled) {
                            this.sendMessage(value.toString())
                        }
                    }
                    "exception" -> {
                        Log.e(type.uppercase(), value as String)
                        this.sendMessage(value)
                    }
                }
            }

            UniversalReaderTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            colors =
                                TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary,
                                ),
                            title = { Text("Universal Reader") },
                            actions = {
                                Box {
                                    IconButton(onClick = { modeMenuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "Configure display parameters"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = modeMenuExpanded,
                                        onDismissRequest = { modeMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            { Text("Load configuration") },
                                            onClick = {
                                                configurationFileLauncher.launch("application/json")
                                            },
                                            trailingIcon = { Icons.Outlined.Create }
                                        )

                                        Divider()
                                        DropdownMenuItem(
                                            { Text("Logs") },
                                            onClick = { logsEnabled = !logsEnabled },
                                            trailingIcon = {
                                                Checkbox(
                                                    checked = logsEnabled,
                                                    onCheckedChange = { logsEnabled = it }
                                                )
                                            }
                                        )

                                        Divider()

                                        DropdownMenuItem(
                                            { Text("Commands") },
                                            onClick = { commandsEnabled = !commandsEnabled },
                                            trailingIcon = {
                                                Checkbox(
                                                    checked = commandsEnabled,
                                                    onCheckedChange = { commandsEnabled = it }
                                                )
                                            }
                                        )

                                        Divider()

                                        DropdownMenuItem(
                                            { Text("Responses") },
                                            onClick = { responsesEnabled = !responsesEnabled },
                                            trailingIcon = {
                                                Checkbox(
                                                    checked = responsesEnabled,
                                                    onCheckedChange = { responsesEnabled = it }
                                                )
                                            }
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            sendMessage(null)
                                            snackbarHostState.showSnackbar(
                                                "History cleared",
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Localized description"
                                    )
                                }
                            },
                        )
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) { Main() }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        enableReader()
    }

    private fun enableReader(
        initialFlags: Int = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_F
    ) {
        val flags =
            (initialFlags or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS)

        nfcAdapter?.enableReaderMode(this, this, flags, null)
        nfcAdapter?.isSecureNfcEnabled()
    }

    private fun disableReader() {
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onPause() {
        super.onPause()

        disableReader()
    }

    override fun onTagDiscovered(tag: Tag) {
        sendMessage(null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                hook("log", "Got tag: ${tag.id.toHexString()} ${tag.techList.contentToString()}")

                val isoDep = IsoDep.get(tag) ?: return@launch
                isoDep.connect()

                val result = configuration.read(isoDep, hook)
                vasDone = true
                sendReadData(result)

                isoDep.close()
            } catch (e: Exception) {
                sendMessage("Got an exception: ${e}")
                Log.e("MainActivity", "${e.stackTraceToString()}")
            }
        }
    }

    fun sendMessage(message: String?) {
        val intent = Intent(NFC_LOG_MESSAGE)
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    fun sendReadData(read: ValueAddedServicesResult) {
        Log.i("MainActivity", "sendReadData=${read}")
        try {
            when (read) {
                is VasResult -> {
                    for (t in read.read) {
                        val intent = Intent(VAS_READ_MESSAGE)
                        intent.putExtra(
                            "read",
                            arrayOf(
                                t.passTypeIdentifier,
                                t.status.toString(),
                                t.payload?.value.toString()
                            )
                        )
                        sendBroadcast(intent)
                    }
                }
                is SmartTapResult -> {
                    for (o in read.objects) {
                        val intent = Intent(VAS_READ_MESSAGE)
                        when (o) {
                            is SmartTapObjectPass -> {
                                intent.putExtra(
                                    "read",
                                    arrayOf(o.objectId.toHexString(), o.type, o.message)
                                )
                                sendBroadcast(intent)
                            }
                            is SmartTapObjectCustomer -> {
                                intent.putExtra(
                                    "read",
                                    arrayOf(o.customerId.toHexString(), "CUSTOMER", o.language)
                                )
                                sendBroadcast(intent)
                            }
                        }
                    }
                }
                else -> Unit
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "${e.stackTraceToString()}")
        }
    }
}

@Composable
fun MessageBroadcastReceiver(systemAction: String, onSuccess: (Intent?) -> Unit) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(onSuccess)

    DisposableEffect(context) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    currentOnSystemEvent(intent)
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcast, intentFilter, Context.RECEIVER_EXPORTED)
            Log.i("DisposableEffect", "registered receiver")
        } else {
            context.registerReceiver(broadcast, intentFilter)
        }

        onDispose { context.unregisterReceiver(broadcast) }
    }
}

@Composable
fun Main() {
    var messages: List<Triple<Int, String, Array<String>>> by remember { mutableStateOf(listOf()) }

    MessageBroadcastReceiver(NFC_LOG_MESSAGE) { intent ->
        val message = intent?.getStringExtra("message")
        if (message != null) {
            messages =
                (messages.toMutableList() + Triple(messages.size, "message", arrayOf(message)))
                    .toList()
        } else {
            messages = emptyList()
        }
    }

    MessageBroadcastReceiver(VAS_READ_MESSAGE) { intent ->
        val read = intent?.getStringArrayExtra("read") ?: return@MessageBroadcastReceiver
        Log.i("MessageBroadcastReceiver", "read=${read.contentToString()}")
        messages = (messages.toMutableList() + Triple(messages.size, "read", read)).toList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        userScrollEnabled = true
    ) {
        items(messages.size, { index -> messages.get(index).first }) { result ->
            val message = messages.get(result)
            when (message.second) {
                "read" ->
                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = message.third[0],
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )

                            Text(
                                text = message.third[1],
                            )
                        }

                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.8f),
                        )
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = message.third[2],
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                        }
                    }
                else ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = message.third[0], modifier = Modifier.weight(1f).fillMaxWidth())
                    }
            }
        }
    }
}
