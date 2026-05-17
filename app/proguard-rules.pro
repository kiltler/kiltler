# Room
-keep class androidx.room.** { *; }

# Gson — модели бэкапа сериализуются по именам полей
-keepclassmembers class com.kiltler.assistant.data.** { <fields>; }
-keep class com.kiltler.assistant.backup.** { *; }

# osmdroid
-keep class org.osmdroid.** { *; }
