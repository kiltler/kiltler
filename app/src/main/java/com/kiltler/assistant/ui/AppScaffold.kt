package com.kiltler.assistant.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kiltler.assistant.backup.BackupManager
import com.kiltler.assistant.data.Material
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.ui.screens.MapScreen
import com.kiltler.assistant.ui.screens.MaterialsScreen
import com.kiltler.assistant.ui.screens.OrdersScreen
import com.kiltler.assistant.ui.screens.ScheduleScreen
import kotlinx.coroutines.launch

private enum class Tab(val title: String, val icon: ImageVector) {
    SCHEDULE("Расписание", Icons.Default.Schedule),
    ORDERS("Заказы", Icons.AutoMirrored.Filled.List),
    MATERIALS("Материалы", Icons.Default.Inventory2),
    MAP("Карта", Icons.Default.Map)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val vm: AssistantViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(Tab.SCHEDULE) }
    var menuOpen by remember { mutableStateOf(false) }

    var showReminderDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<Order?>(null) }
    var showMaterialDialog by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<Material?>(null) }

    var routeRequest by remember { mutableStateOf<String?>(null) }

    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val orders by vm.orders.collectAsStateWithLifecycle()
    val materials by vm.materials.collectAsStateWithLifecycle()
    val workPlaces by vm.workPlaces.collectAsStateWithLifecycle()

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val json = BackupManager.readJson(context, uri)
                vm.importBackup(json) { ok, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Не удалось открыть файл", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        tab.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Поделиться данными") },
                            onClick = {
                                menuOpen = false
                                scope.launch {
                                    val backup = vm.buildBackup()
                                    val uri = BackupManager.writeShareFile(context, backup)
                                    context.startActivity(
                                        Intent.createChooser(
                                            BackupManager.shareIntent(uri),
                                            "Поделиться резервной копией"
                                        )
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Импорт из файла") },
                            onClick = {
                                menuOpen = false
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Расходники: сплит-система") },
                            onClick = {
                                menuOpen = false
                                vm.addSplitSystemMaterials()
                                Toast.makeText(
                                    context,
                                    "Расходники добавлены в «Материалы»",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { tab = entry },
                        icon = { Icon(entry.icon, contentDescription = entry.title) },
                        label = {
                            Text(
                                entry.title,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab != Tab.MAP) {
                FloatingActionButton(onClick = {
                    when (tab) {
                        Tab.SCHEDULE -> { editingReminder = null; showReminderDialog = true }
                        Tab.ORDERS -> { editingOrder = null; showOrderDialog = true }
                        Tab.MATERIALS -> { editingMaterial = null; showMaterialDialog = true }
                        Tab.MAP -> {}
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                    Tab.SCHEDULE -> ScheduleScreen(
                        reminders = reminders,
                        editing = editingReminder,
                        showDialog = showReminderDialog,
                        onDismissDialog = { showReminderDialog = false },
                        onSave = vm::saveReminder,
                        onToggleDone = vm::toggleReminderDone,
                        onDelete = vm::deleteReminder
                    )
                    Tab.ORDERS -> OrdersScreen(
                        orders = orders,
                        editing = editingOrder,
                        showDialog = showOrderDialog,
                        onDismissDialog = { showOrderDialog = false },
                        onSave = vm::saveOrder,
                        onDelete = vm::deleteOrder,
                        onEdit = { editingOrder = it; showOrderDialog = true },
                        onRoute = { order ->
                            if (order.address.isNotBlank()) {
                                routeRequest = order.address
                                tab = Tab.MAP
                            }
                        }
                    )
                    Tab.MATERIALS -> MaterialsScreen(
                        materials = materials,
                        editing = editingMaterial,
                        showDialog = showMaterialDialog,
                        onDismissDialog = { showMaterialDialog = false },
                        onSave = vm::saveMaterial,
                        onDelete = vm::deleteMaterial,
                        onEdit = { editingMaterial = it; showMaterialDialog = true },
                        onAdjust = vm::adjustMaterial
                    )
                    Tab.MAP -> MapScreen(
                        workPlaces = workPlaces,
                        onSave = vm::saveWorkPlace,
                        onSaveReminder = vm::saveReminder,
                        onDelete = vm::deleteWorkPlace,
                        routeRequest = routeRequest,
                        onRouteConsumed = { routeRequest = null }
                    )
                }
            }
        }
    }
