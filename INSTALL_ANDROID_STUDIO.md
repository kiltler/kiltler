# Установка приложения «Ассистент» через Android Studio

Гайд по сборке и запуску этого проекта в Android Studio.

Приложение написано на **Kotlin + Jetpack Compose**, использует базу
данных **Room**, карты **osmdroid** и локальные уведомления.

## 1. Требования

| Компонент | Версия |
|---|---|
| Android Studio | **Ladybug (2024.2.1)** или новее |
| JDK | **17** (входит в Android Studio) |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.9 (скачается автоматически через wrapper) |
| Kotlin | 2.0.21 |
| Android SDK | API 35 (compileSdk / targetSdk), minSdk — API 26 (Android 8.0) |

Свободное место: примерно 8–10 ГБ (IDE + SDK + эмулятор).

## 2. Установка Android Studio

1. Скачайте Android Studio: <https://developer.android.com/studio>
2. Запустите установщик, на шаге **Install Type** выберите **Standard** —
   IDE сама докачает Android SDK и эмулятор.
3. Дождитесь окончания первичной загрузки компонентов.

## 3. Получение исходного кода

### Вариант А — клонировать прямо из Android Studio

1. Экран **Welcome** → **Get from VCS**.
2. URL репозитория:

   ```
   https://github.com/kiltler/kiltler.git
   ```

3. Выберите папку и нажмите **Clone**.

### Вариант Б — через терминал

```bash
git clone https://github.com/kiltler/kiltler.git
cd kiltler
```

Затем в Android Studio: **File → Open** и выберите папку проекта.

## 4. Синхронизация проекта (Gradle Sync)

При первом открытии Android Studio запустит **Gradle Sync** — скачает
Gradle 8.9 и все зависимости (Compose, Room, osmdroid, gson).

- Дождитесь сообщения **"Gradle sync finished"**.
- Если синхронизация не стартовала — нажмите
  **Sync Project with Gradle Files** (значок слона на панели).
- При запросе установить недостающий **SDK Platform 35** или
  **Build Tools** — соглашайтесь.

Проверьте, что используется JDK 17:
**Settings → Build, Execution, Deployment → Build Tools → Gradle →
Gradle JDK** → выберите версию **17**.

## 5. Устройство для запуска

minSdk проекта — **API 26 (Android 8.0)**, поэтому устройство или
эмулятор должны быть не ниже этой версии.

### Эмулятор

1. **Tools → Device Manager → Create Device**.
2. Выберите модель (например, Pixel 7) и образ системы с API ≥ 26
   (рекомендуется API 34/35). При необходимости скачайте образ.
3. Завершите создание устройства.

### Физическое устройство

1. Настройки → О телефоне → 7 раз нажмите **Номер сборки**.
2. В **Для разработчиков** включите **Отладку по USB**.
3. Подключите телефон кабелем и подтвердите доверие компьютеру.

## 6. Сборка и запуск

1. В выпадающем списке вверху выберите устройство.
2. Нажмите **Run** (зелёный треугольник) или `Shift + F10`.
3. Android Studio соберёт APK и установит приложение.

При первом запуске приложение запросит разрешения — для полной работы
функций понадобятся: микрофон, геолокация, уведомления и точные
будильники (используются для напоминаний по расписанию).

## 7. Сборка APK через терминал

Из корня проекта:

```bash
./gradlew assembleDebug      # отладочный APK
./gradlew assembleRelease    # релизный APK (без подписи)
```

Готовый файл: `app/build/outputs/apk/`.

> На Windows вместо `./gradlew` используйте `gradlew.bat`.

## 8. Структура проекта

```
app/src/main/java/com/kiltler/assistant/
├── MainActivity.kt          точка входа
├── AssistantApp.kt          класс Application
├── data/                    Room: Entities, Daos, Repository, AppDatabase
├── ui/                      Compose-экраны и ViewModel
│   └── screens/             Расписание, Заказы, Материалы, Карта
├── notifications/           напоминания и приёмники (alarm, boot)
└── backup/                  резервное копирование данных
```

## 9. Частые проблемы

| Проблема | Решение |
|---|---|
| Gradle sync падает | **File → Invalidate Caches / Restart** |
| Неверный JDK | Установите Gradle JDK = 17 (см. шаг 4) |
| Нет SDK Platform 35 | **Tools → SDK Manager** → установите API 35 |
| Ошибка KSP / Room | Убедитесь, что синхронизация прошла полностью |
| Устройство не видно | Проверьте отладку по USB; выполните `adb devices` |
| Карта пустая | Проверьте интернет и выданное разрешение на геолокацию |
| Долгая первая сборка | Это нормально — далее сборки кэшируются |
