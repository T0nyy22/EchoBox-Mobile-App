package com.example.echobox

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CapsuleListActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_LOCKED = "locked"
        const val MODE_UNLOCKED = "unlocked"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLockedMode = intent.getStringExtra(EXTRA_MODE) != MODE_UNLOCKED

        val database = MessagesDatabase.getInstance(applicationContext)
        val dao = database.messagesDao()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val viewModel: CapsuleListViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CapsuleListViewModel(
                                dao,
                                isLockedMode
                            ) as T
                        }
                    }
                )

                CapsuleListScreen(
                    viewModel = viewModel,
                    isLockedMode = isLockedMode,
                    onBack = { finish() }
                )
            }
        }
    }
}

class CapsuleListViewModel(
    private val dao: MessagesDao,
    private val isLockedMode: Boolean
) : ViewModel() {

    private val _messages =
        MutableStateFlow<List<MessagesEntity>>(emptyList())

    val messages: StateFlow<List<MessagesEntity>> =
        _messages.asStateFlow()

    init {
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            _messages.value = if (isLockedMode) {
                dao.getLockedMessages(now)
            } else {
                dao.getUnlockedMessages(now)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleListScreen(
    viewModel: CapsuleListViewModel,
    isLockedMode: Boolean,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isLockedMode) "Locked Capsules" else "Unlocked Capsules")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isLockedMode) {
                            "No locked capsules."
                        } else {
                            "No unlocked capsules yet."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { message ->
                        if (isLockedMode) {
                            LockedCapsuleCard(message = message)
                        } else {
                            UnlockedCapsuleCard(
                                message = message,
                                onClick = {
                                    if (message.isUnlocked) {
                                        val intent = Intent(context, MessageDetailsActivity::class.java)
                                        intent.putExtra("message_id", message.id)
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Open this capsule from the main screen first.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LockedCapsuleCard(message: MessagesEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = message.title, style = MaterialTheme.typography.titleMedium)

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun UnlockedCapsuleCard(message: MessagesEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = "Unlocked",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Category: ${message.category}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (!message.isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✨ Ready to open from the main screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}