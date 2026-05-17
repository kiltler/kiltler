package com.kiltler.assistant.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.data.WorkPlace
import com.kiltler.assistant.ui.GeocodeResult
import com.kiltler.assistant.ui.VoiceTextField
import com.kiltler.assistant.ui.formatDate
import com.kiltler.assistant.ui.formatDateTime
import com.kiltler.assistant.ui.geocodeAddress
import com.kiltler.assistant.ui.pickDateTime
import com.kiltler.assistant.ui.rememberVoiceInput
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(
    workPlaces: List<WorkPlace>,
    onSave: (WorkPlace) -> Unit,
    onSaveReminder: (Reminder) -> Unit,
    onDelete: (WorkPlace) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var pendingPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedPlace by remember { mutableStateOf<WorkPlace?>(null) }
    var voiceAddress by remember { mutableStateOf<String?>(null) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setUseDataConnection(true)
            controller.setZoom(11.0)
            val start = workPlaces.firstOrNull()
                ?.let { GeoPoint(it.latitude, it.longitude) }
                ?: GeoPoint(55.751244, 37.618423)
            controller.setCenter(start)

            val receiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                override fun longPressHelper(p: GeoPoint?): Boolean {
                    if (p != null) pendingPoint = p
                    return true
                }
            }
            overlays.add(MapEventsOverlay(receiver))
        }
    }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            enableMyLocation(mapView, locationOverlay)
        }
    }

    val startVoiceAddress = rememberVoiceInput { spoken -> voiceAddress = spoken }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.removeAll { it is Marker }
                workPlaces.forEach { place ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(place.latitude, place.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = place.title
                        snippet = place.description
                        setOnMarkerClickListener { _, _ ->
                            selectedPlace = place
                            true
                        }
                    }
                    map.overlays.add(marker)
                }
                map.invalidate()
            }
        )

        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
        ) {
            Text(
                "Удержите палец на карте или нажмите «Адрес голосом»",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }

        FloatingActionButton(
            onClick = {
                val fine = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (fine) {
                    enableMyLocation(mapView, locationOverlay)
                } else {
                    locationPermission.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Моё местоположение")
        }

        ExtendedFloatingActionButton(
            onClick = startVoiceAddress,
            icon = { Icon(Icons.Default.Mic, contentDescription = null) },
            text = { Text("Адрес голосом") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    pendingPoint?.let { point ->
        AddPlaceDialog(
            point = point,
            onDismiss = { pendingPoint = null },
            onSave = { place ->
                onSave(place)
                pendingPoint = null
            }
        )
    }

    selectedPlace?.let { place ->
        PlaceDetailsDialog(
            place = place,
            onDismiss = { selectedPlace = null },
            onDelete = {
                onDelete(place)
                selectedPlace = null
            }
        )
    }

    voiceAddress?.let { text ->
        VoiceAddressDialog(
            initialText = text,
            onDismiss = { voiceAddress = null },
            onConfirm = { place, reminder ->
                onSave(place)
                reminder?.let(onSaveReminder)
                mapView.controller.animateTo(GeoPoint(place.latitude, place.longitude))
                mapView.controller.setZoom(16.0)
                voiceAddress = null
            }
        )
    }
}

private fun enableMyLocation(mapView: MapView, overlay: MyLocationNewOverlay) {
    overlay.enableMyLocation()
    if (!mapView.overlays.contains(overlay)) {
        mapView.overlays.add(overlay)
    }
    overlay.runOnFirstFix {
        val location = overlay.myLocation
        if (location != null) {
            mapView.post {
                mapView.controller.animateTo(location)
                mapView.controller.setZoom(15.0)
            }
        }
    }
    mapView.invalidate()
}

@Composable
private fun AddPlaceDialog(
    point: GeoPoint,
    onDismiss: () -> Unit,
    onSave: (WorkPlace) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Место работы") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Координаты: %.5f, %.5f".format(point.latitude, point.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                VoiceTextField(title, { title = it }, "Название / адрес", Modifier.fillMaxWidth())
                VoiceTextField(
                    description, { description = it }, "Заметка",
                    Modifier.fillMaxWidth(), singleLine = false, minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            WorkPlace(
                                title = title.trim(),
                                description = description.trim(),
                                latitude = point.latitude,
                                longitude = point.longitude
                            )
                        )
                    }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

/**
 * Комплексный голосовой ввод адреса: распознанный текст геокодируется
 * в координаты, после чего создаётся точка на карте и (по желанию)
 * напоминание с этим адресом.
 */
@Composable
private fun VoiceAddressDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (WorkPlace, Reminder?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var address by remember { mutableStateOf(initialText) }
    var note by remember { mutableStateOf("") }
    var found by remember { mutableStateOf<GeocodeResult?>(null) }
    var status by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var withReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf(System.currentTimeMillis() + 3_600_000L) }

    fun runSearch() {
        if (address.isBlank()) {
            found = null
            status = "Введите или продиктуйте адрес"
            return
        }
        searching = true
        status = ""
        found = null
        scope.launch {
            val result = geocodeAddress(context, address)
            searching = false
            if (result != null) {
                found = result
                status = "Найдено: %.5f, %.5f".format(result.latitude, result.longitude)
            } else {
                status = "Адрес не найден. Уточните формулировку и повторите."
            }
        }
    }

    LaunchedEffect(Unit) { runSearch() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Адрес голосом") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VoiceTextField(address, { address = it }, "Адрес", Modifier.fillMaxWidth())

                OutlinedButton(
                    onClick = { runSearch() },
                    enabled = !searching,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Text("  Найти на карте")
                }

                if (searching) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text("  Поиск адреса…", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (status.isNotBlank()) {
                    Text(
                        status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (found != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }

                VoiceTextField(
                    note, { note = it }, "Заметка (необязательно)",
                    Modifier.fillMaxWidth(), singleLine = false, minLines = 2
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = withReminder, onCheckedChange = { withReminder = it })
                    Text("  Напоминание", style = MaterialTheme.typography.bodyMedium)
                }
                if (withReminder) {
                    OutlinedButton(
                        onClick = { pickDateTime(context, reminderTime) { reminderTime = it } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Text("  ${formatDateTime(reminderTime)}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = found != null && !searching,
                onClick = {
                    val result = found ?: return@TextButton
                    val title = address.trim().ifBlank { result.displayName }
                    val place = WorkPlace(
                        title = title,
                        description = note.trim(),
                        latitude = result.latitude,
                        longitude = result.longitude
                    )
                    val reminder = if (withReminder) {
                        Reminder(
                            title = title,
                            description = listOfNotNull(
                                note.trim().ifBlank { null },
                                result.displayName
                            ).joinToString(" — "),
                            timeMillis = reminderTime
                        )
                    } else null
                    onConfirm(place, reminder)
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun PlaceDetailsDialog(
    place: WorkPlace,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(place.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (place.description.isNotBlank()) Text(place.description)
                Text(
                    "Координаты: %.5f, %.5f".format(place.latitude, place.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Добавлено: ${formatDate(place.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}
