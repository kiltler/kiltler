package com.kiltler.assistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SettingsDialog(
    initial: AppSettings,
    onDismiss: () -> Unit,
    onSave: (AppSettings) -> Unit
) {
    var cleaning by remember { mutableStateOf(initial.cleaningPrice.priceText()) }
    var refill by remember { mutableStateOf(initial.refillPrice.priceText()) }
    var install by remember { mutableStateOf(initial.installPrice.priceText()) }
    var preInstall by remember { mutableStateOf(initial.preInstallPrice.priceText()) }
    var demount by remember { mutableStateOf(initial.demountPrice.priceText()) }
    var mat by remember { mutableStateOf(initial.materialsPct.toString()) }
    var ads by remember { mutableStateOf(initial.adsPct.toString()) }
    var acMdv7 by remember { mutableStateOf(initial.acPriceMdv7.priceText()) }
    var acMdv9 by remember { mutableStateOf(initial.acPriceMdv9.priceText()) }
    var acMdv12 by remember { mutableStateOf(initial.acPriceMdv12.priceText()) }
    var acMdv24 by remember { mutableStateOf(initial.acPriceMdv24.priceText()) }
    var acMulti by remember { mutableStateOf(initial.acPriceMulti.priceText()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionTitle("Цены за работу, ₽")
                PriceField(cleaning, { cleaning = it }, "Чистка")
                PriceField(refill, { refill = it }, "Заправка")
                PriceField(install, { install = it }, "Установка")
                PriceField(preInstall, { preInstall = it }, "Закладка")
                PriceField(demount, { demount = it }, "Демонтаж")

                HorizontalDivider()
                SectionTitle("Цены кондиционеров (закупка), ₽")
                Text(
                    "Используются как себестоимость при продаже. В прибыль идёт только наценка, которую вы вводите в заказе.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PriceField(acMdv7, { acMdv7 = it }, "MDV 7")
                PriceField(acMdv9, { acMdv9 = it }, "MDV 9")
                PriceField(acMdv12, { acMdv12 = it }, "MDV 12")
                PriceField(acMdv24, { acMdv24 = it }, "MDV 24")
                PriceField(acMulti, { acMulti = it }, "Мультисплит")

                HorizontalDivider()
                SectionTitle("Отчисления, % от выручки")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mat,
                        onValueChange = { mat = it.filter(Char::isDigit).take(3) },
                        label = { Text("Материалы, %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ads,
                        onValueChange = { ads = it.filter(Char::isDigit).take(3) },
                        label = { Text("Реклама, %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    AppSettings(
                        cleaningPrice = cleaning.parsePrice(),
                        refillPrice = refill.parsePrice(),
                        installPrice = install.parsePrice(),
                        preInstallPrice = preInstall.parsePrice(),
                        demountPrice = demount.parsePrice(),
                        materialsPct = (mat.toIntOrNull() ?: 0).coerceIn(0, 100),
                        adsPct = (ads.toIntOrNull() ?: 0).coerceIn(0, 100),
                        acPriceMdv7 = acMdv7.parsePrice(),
                        acPriceMdv9 = acMdv9.parsePrice(),
                        acPriceMdv12 = acMdv12.parsePrice(),
                        acPriceMdv24 = acMdv24.parsePrice(),
                        acPriceMulti = acMulti.parsePrice()
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun PriceField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(7)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun Double.priceText(): String = if (this > 0) toLong().toString() else ""

private fun String.parsePrice(): Double = toDoubleOrNull() ?: 0.0
