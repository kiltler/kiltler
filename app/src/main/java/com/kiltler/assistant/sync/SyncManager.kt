package com.kiltler.assistant.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Облачная синхронизация данных через Firebase Firestore.
 *
 * Оба телефона используют один «код синхронизации» — он задаёт документ
 * в коллекции `sync`, через который данные переносятся между устройствами.
 */
class SyncManager(context: Context) {

    private val prefs = context.getSharedPreferences("sync", Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listener: ListenerRegistration? = null
    private var lastJson: String? = null

    var code: String?
        get() = prefs.getString("code", null)?.takeIf { it.isNotBlank() }
        private set(value) {
            prefs.edit().putString("code", value).apply()
        }

    val enabled: Boolean get() = code != null

    /**
     * Подписывается на изменения документа синхронизации.
     * [onRemote] — пришли данные из облака; [onEmpty] — облако ещё пустое.
     */
    fun start(onRemote: (String) -> Unit, onEmpty: () -> Unit) {
        val current = code ?: return
        auth.signInAnonymously().addOnCompleteListener {
            listener?.remove()
            listener = firestore.collection("sync").document(current)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null || snapshot.metadata.hasPendingWrites()) {
                        return@addSnapshotListener
                    }
                    val json = snapshot.getString("data")
                    when {
                        json == null -> onEmpty()
                        json != lastJson -> {
                            lastJson = json
                            onRemote(json)
                        }
                    }
                }
        }
    }

    fun setCode(newCode: String, onRemote: (String) -> Unit, onEmpty: () -> Unit) {
        code = newCode.trim()
        lastJson = null
        start(onRemote, onEmpty)
    }

    fun disable() {
        listener?.remove()
        listener = null
        lastJson = null
        code = null
    }

    /** Выгружает снимок данных в облако. */
    fun push(json: String) {
        val current = code ?: return
        lastJson = json
        firestore.collection("sync").document(current)
            .set(mapOf("data" to json, "updatedAt" to System.currentTimeMillis()))
    }
}
