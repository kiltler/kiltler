package com.kiltler.assistant.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kiltler.assistant.data.WorkPlace
import com.kiltler.assistant.ui.VoiceTextField
import com.kiltler.assistant.ui.formatDate
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
    onDelete: (WorkPlace) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var pendingPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedPlace by remember { mutableStateOf<WorkPlace?>(null) }

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
                "Удержите палец на карте, чтобы отметить место работы",
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
