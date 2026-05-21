package com.kiltler.assistant.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
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
import com.kiltler.assistant.R
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.OrderStatus
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.data.WorkPlace
import com.kiltler.assistant.ui.GeocodeResult
import com.kiltler.assistant.ui.KhabarovskRegion
import com.kiltler.assistant.ui.VoiceTextField
import com.kiltler.assistant.ui.formatDate
import com.kiltler.assistant.ui.formatDateTime
import com.kiltler.assistant.ui.formatTime
import com.kiltler.assistant.ui.geocodeAddress
import com.kiltler.assistant.ui.isToday
import com.kiltler.assistant.ui.openYandexDrivingRoute
import com.kiltler.assistant.ui.pickDateTime
import com.kiltler.assistant.ui.rememberVoiceInput
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch

private const val HOUR_MS = 3_600_000L

@Composable
fun MapScreen(
    workPlaces: List<WorkPlace>,
    orders: List<Order>,
    dayFilter: Long?,
    onSave: (WorkPlace) -> Unit,
    onSaveReminder: (Reminder) -> Unit,
    onDelete: (WorkPlace) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedPlace by remember { mutableStateOf<WorkPlace?>(null) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var voiceAddress by remember { mutableStateOf<String?>(null) }
    var routeActive by remember { mutableStateOf(false) }
    var orderPoints by remember { mutableStateOf<List<Pair<Order, Point>>>(emptyList()) }
    val geocodeCache = remember { mutableMapOf<String, Point>() }

    val mapView = remember { MapView(context) }
    val map = remember { mapView.mapWindow.map }
    val placemarks = remember { map.mapObjects.addCollection() }
    val routes = remember { map.mapObjects.addCollection() }
    val userLocationLayer = remember {
        MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow).apply {
            setVisible(true)
        }
    }
    val drivingRouter = remember {
        DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
    }
    val pinBlue = remember { pinImageProvider(context, R.drawable.ic_map_pin) }
    val pinAmber = remember { pinImageProvider(context, R.drawable.ic_map_pin_amber) }
    val pinRed = remember { pinImageProvider(context, R.drawable.ic_map_pin_red) }
    var drivingSession by remember { mutableStateOf<DrivingSession?>(null) }

    fun pinForOrder(scheduledMillis: Long?): ImageProvider {
        if (scheduledMillis == null) return pinBlue
        val left = scheduledMillis - System.currentTimeMillis()
        return when {
            left <= 3 * HOUR_MS -> pinRed
            left <= 12 * HOUR_MS -> pinAmber
            else -> pinBlue
        }
    }

    val placemarkTapListener = remember {
        MapObjectTapListener { mapObject, _ ->
            when (val data = mapObject.userData) {
                is WorkPlace -> selectedPlace = data
                is Order -> selectedOrder = data
            }
            true
        }
    }
    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) = Unit
            override fun onMapLongTap(map: Map, point: Point) = Unit
        }
    }
    val routeListener = remember {
        object : DrivingSession.DrivingRouteListener {
            override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
                routes.clear()
                val route = drivingRoutes.firstOrNull() ?: return
                routes.addPolyline(route.geometry).apply {
                    setStrokeColor(0xFF0A6CCC.toInt())
                    style = style.apply { strokeWidth = 5f }
                }
                route.geometry.points.lastOrNull()?.let { end ->
                    routes.addPlacemark().apply {
                        geometry = end
                        setIcon(pinBlue, IconStyle().apply { anchor = PointF(0.5f, 1.0f) })
                    }
                }
                routeActive = true
            }

            override fun onDrivingRoutesError(error: com.yandex.runtime.Error) {
                Toast.makeText(context, "Не удалось построить маршрут", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun buildRouteTo(destination: Point) {
        val userPoint = userLocationLayer.cameraPosition()?.target
        if (userPoint == null) {
            Toast.makeText(
                context,
                "Геолокация не определена — маршрут от центра карты",
                Toast.LENGTH_SHORT
            ).show()
        }
        val from = userPoint ?: map.cameraPosition.target
        val requestPoints = listOf(
            RequestPoint(from, RequestPointType.WAYPOINT, null, null, null),
            RequestPoint(destination, RequestPointType.WAYPOINT, null, null, null)
        )
        drivingSession?.cancel()
        drivingSession = drivingRouter.requestRoutes(
            requestPoints, DrivingOptions(), VehicleOptions(), routeListener
        )
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            userLocationLayer.setVisible(true)
        }
    }

    val startVoiceAddress = rememberVoiceInput { spoken -> voiceAddress = spoken }

    LaunchedEffect(Unit) {
        map.addInputListener(inputListener)
        val start = workPlaces.firstOrNull()
            ?.let { Point(it.latitude, it.longitude) }
            ?: Point(KhabarovskRegion.CENTER_LAT, KhabarovskRegion.CENTER_LON)
        map.move(CameraPosition(start, 12f, 0f, 0f))
    }

    // Заказы с адресом дублируются на карте отдельными метками.
    LaunchedEffect(orders) {
        val collected = mutableListOf<Pair<Order, Point>>()
        orders
            .filter { order ->
                order.address.isNotBlank() &&
                    OrderStatus.from(order.status).let {
                        it != OrderStatus.DONE && it != OrderStatus.CANCELLED
                    }
            }
            .forEach { order ->
                val point = geocodeCache[order.address]
                    ?: geocodeAddress(context, order.address)?.let { Point(it.latitude, it.longitude) }
                if (point != null) {
                    geocodeCache[order.address] = point
                    collected.add(order to point)
                    orderPoints = collected.toList()
                }
            }
        orderPoints = collected.toList()
    }

    DisposableEffect(lifecycleOwner) {
        // Экран открывается, когда жизненный цикл уже в STARTED, поэтому
        // запускаем карту сразу — иначе MapKit не начнёт отрисовку.
        mapView.onStart()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onStop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = {
                placemarks.clear()
                workPlaces.forEach { place ->
                    placemarks.addPlacemark().apply {
                        geometry = Point(place.latitude, place.longitude)
                        userData = place
                        setIcon(pinBlue, IconStyle().apply { anchor = PointF(0.5f, 1.0f) })
                        addTapListener(placemarkTapListener)
                    }
                }
                val dayEnd = dayFilter?.let { it + 24L * 60 * 60 * 1000 }
                orderPoints.forEach { (order, point) ->
                    val scheduled = order.scheduledMillis
                    if (dayFilter != null && (scheduled == null ||
                            scheduled !in dayFilter until dayEnd!!)) {
                        return@forEach
                    }
                    placemarks.addPlacemark().apply {
                        geometry = point
                        userData = order
                        setIcon(
                            pinForOrder(scheduled),
                            IconStyle().apply { anchor = PointF(0.5f, 1.0f) }
                        )
                        val showTime = scheduled != null &&
                            (dayFilter != null || isToday(scheduled))
                        if (showTime) {
                            setText(
                                "(${formatTime(scheduled!!)})",
                                TextStyle().apply {
                                    size = 11f
                                    placement = TextStyle.Placement.TOP
                                    offset = 4f
                                }
                            )
                        }
                        addTapListener(placemarkTapListener)
                    }
                }
            }
        )

        Text(
            "Заказы дня — нажмите на метку, чтобы открыть маршрут",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        )

        FloatingActionButton(
            onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    locationPermission.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                    return@FloatingActionButton
                }
                userLocationLayer.setVisible(true)
                val position = userLocationLayer.cameraPosition()
                if (position != null) {
                    map.move(
                        CameraPosition(position.target, 16f, 0f, 0f),
                        Animation(Animation.Type.SMOOTH, 0.6f),
                        null
                    )
                } else {
                    Toast.makeText(
                        context, "Определяю местоположение…", Toast.LENGTH_SHORT
                    ).show()
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

        if (routeActive) {
            ExtendedFloatingActionButton(
                onClick = {
                    drivingSession?.cancel()
                    routes.clear()
                    routeActive = false
                },
                icon = { Icon(Icons.Default.Close, contentDescription = null) },
                text = { Text("Сбросить маршрут") },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
            )
        }
    }

    selectedPlace?.let { place ->
        PlaceDetailsDialog(
            place = place,
            onDismiss = { selectedPlace = null },
            onRoute = {
                buildRouteTo(Point(place.latitude, place.longitude))
                selectedPlace = null
            },
            onDelete = {
                onDelete(place)
                selectedPlace = null
            }
        )
    }

    selectedOrder?.let { order ->
        val point = orderPoints.firstOrNull { it.first.id == order.id }?.second
        OrderMarkerDialog(
            order = order,
            onDismiss = { selectedOrder = null },
            onRoute = {
                if (point != null) {
                    openYandexDrivingRoute(context, point.latitude, point.longitude)
                }
                selectedOrder = null
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
                map.move(
                    CameraPosition(Point(place.latitude, place.longitude), 16f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0.6f),
                    null
                )
                voiceAddress = null
            }
        )
    }
}

/** Растеризует векторную метку в bitmap для иконки MapKit. */
private fun pinImageProvider(context: Context, resId: Int): ImageProvider {
    val drawable = ContextCompat.getDrawable(context, resId)!!
    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return ImageProvider.fromBitmap(bitmap)
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
    var reminderTime by remember { mutableStateOf(System.currentTimeMillis() + HOUR_MS) }

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
    onRoute: () -> Unit,
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
                OutlinedButton(
                    onClick = onRoute,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Text("  Маршрут сюда")
                }
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

@Composable
private fun OrderMarkerDialog(
    order: Order,
    onDismiss: () -> Unit,
    onRoute: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(order.clientName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (order.address.isNotBlank()) Text(order.address)
                order.scheduledMillis?.let {
                    Text(
                        "Выезд: ${formatDateTime(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(
                    onClick = onRoute,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Text("  Маршрут (Яндекс Карты)")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}
