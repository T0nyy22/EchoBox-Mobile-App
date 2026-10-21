package com.example.echobox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room Database instance using your custom database class
        val database = MessagesDatabase.getInstance(applicationContext)
        val dao = database.messagesDao()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Initialize ViewModel with the DAO factory
                val viewModel: MainViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return MainViewModel(dao) as T
                        }
                    }
                )

                MainScreen(viewModel = viewModel)
            }
        }
    }
}

// ViewModel handling database communication and state management
class MainViewModel(private val dao: MessagesDao) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessagesEntity>>(emptyList())
    val messages: StateFlow<List<MessagesEntity>> = _messages.asStateFlow()

    init {
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _messages.value = dao.getAllMessages()
        }
    }

    fun addMessage(title: String, messageText: String, targetedDate: Long) {
        viewModelScope.launch {
            val newMessage = MessagesEntity(
                title = title,
                message = messageText,
                targetedDate = targetedDate,
                isUnlocked = targetedDate <= System.currentTimeMillis()
            )
            dao.insertMessage(newMessage)
            loadMessages()
        }
    }

    fun unlockMessage(messageEntity: MessagesEntity) {
        if (messageEntity.targetedDate <= System.currentTimeMillis() && !messageEntity.isUnlocked) {
            viewModelScope.launch {
                val updatedMessage = messageEntity.copy(isUnlocked = true)
                dao.updateMessage(updatedMessage)
                loadMessages()
            }
        }
    }

    fun deleteMessage(messageEntity: MessagesEntity) {
        viewModelScope.launch {
            dao.deleteMessage(messageEntity)
            loadMessages()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val messages by viewModel.messages.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Real-time ticker state to force recomposition every second so UI updates instantly when time is reached
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Check every second
            currentTime = System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EchoBox - Future Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Message")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No time capsules found. Tap '+' to create one!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        MessageCard(
                            message = message,
                            currentTime = currentTime,
                            onUnlock = { viewModel.unlockMessage(message) },
                            onDelete = { viewModel.deleteMessage(message) }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddMessageDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { title, content, targetDate ->
                        viewModel.addMessage(title, content, targetDate)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun MessageCard(message: MessagesEntity, currentTime: Long, onUnlock: () -> Unit, onDelete: () -> Unit) {
    val isTimeReached = message.targetedDate <= currentTime
    val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
    val targetDateStr = dateFormat.format(Date(message.targetedDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = message.title, style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = if (message.isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = "Status",
                    tint = if (message.isUnlocked || isTimeReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Target Date: $targetDateStr", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            // Check if it's unlocked in the database
            if (message.isUnlocked) {
                // UNLOCKED: Show the secret message proudly!
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (isTimeReached) {
                // TIME REACHED, BUT NOT YET OPENED BY USER: Show teaser / open prompt
                Text(
                    text = "✨ Time capsule is ready to be opened!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                // STILL LOCKED: Keep it hidden
                Text(
                    text = "🔒 Locked until target date is reached.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show "Open Capsule" button ONLY when time is reached and it hasn't been opened yet
                if (isTimeReached && !message.isUnlocked) {
                    Button(onClick = onUnlock) {
                        Text("Open Capsule")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddMessageDialog(onDismiss: () -> Unit, onSave: (String, String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // Calendar instance to hold the selected target date and time
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) } }
    var targetMillis by remember { mutableStateOf(calendar.timeInMillis) }

    val context = LocalContext.current
    // 12-hour format with AM/PM
    val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Time Capsule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Secret Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Date & Time Picker Trigger Field
                OutlinedTextField(
                    value = dateFormat.format(Date(targetMillis)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Unlock Date & Time") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.MONTH, month)
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            targetMillis = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        false // 12-hour AM/PM clock
                                    ).show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSave(title, content, targetMillis)
                    }
                }
            ) {
                Text("Seal Capsule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}