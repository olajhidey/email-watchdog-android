package app.web.jiniyede.email_watchdog

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// UI State for the summary list
sealed class SummaryUiState {
    object Loading : SummaryUiState()
    data class Success(val summaries: List<EmailSummary>) : SummaryUiState()
    data class Error(val message: String) : SummaryUiState()
}

// One-time UI events
sealed class SummaryEvent {
    data class ShowSnackbar(val message: String) : SummaryEvent()
    object DeleteSuccess : SummaryEvent()
    data class DeleteError(val message: String) : SummaryEvent()
}

class SummaryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.Loading)
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private val _summaries = MutableStateFlow<List<EmailSummary>>(emptyList())
    val summaries: StateFlow<List<EmailSummary>> = _summaries.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _events = MutableSharedFlow<SummaryEvent>()
    val events = _events.asSharedFlow()

    private val db = FirebaseFirestore.getInstance()
    private var snapshotListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        loadSummaries()
    }

    private fun loadSummaries() {
        _uiState.value = SummaryUiState.Loading
        
        snapshotListenerRegistration = db.collection("emails_summaries")
            .addSnapshotListener { snapshots, e ->
                _isRefreshing.value = false
                
                if (e != null) {
                    Log.w("SummaryViewModel", "listen:error", e)
                    _uiState.value = SummaryUiState.Error(
                        e.localizedMessage ?: "Failed to load summaries"
                    )
                    return@addSnapshotListener
                }

                val summaryList = mutableListOf<EmailSummary>()
                for (doc in snapshots!!) {
                    doc.toObject(EmailSummary::class.java).let { summary ->
                        summaryList.add(summary.copy(id = doc.id))
                    }
                }
                _summaries.value = summaryList
                _uiState.value = SummaryUiState.Success(summaryList)
            }
    }

    fun refresh() {
        _isRefreshing.value = true
        // Re-attach snapshot listener to force refresh
        snapshotListenerRegistration?.remove()
        loadSummaries()
    }

    fun getSummaryById(id: String): Flow<EmailSummary?> {
        return summaries.map { list ->
            list.find { it.id == id }
        }
    }

    fun deleteSummary(id: String) {
        viewModelScope.launch {
            db.collection("emails_summaries").document(id)
                .delete()
                .addOnSuccessListener {
                    Log.d("SummaryViewModel", "DocumentSnapshot successfully deleted!")
                    viewModelScope.launch {
                        _events.emit(SummaryEvent.DeleteSuccess)
                        _events.emit(SummaryEvent.ShowSnackbar("Summary deleted"))
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("SummaryViewModel", "Error deleting document", e)
                    viewModelScope.launch {
                        _events.emit(
                            SummaryEvent.DeleteError(
                                e.localizedMessage ?: "Failed to delete summary"
                            )
                        )
                        _events.emit(
                            SummaryEvent.ShowSnackbar(
                                "Failed to delete: ${e.localizedMessage}"
                            )
                        )
                    }
                }
        }
    }

    fun shareSummary(context: Context, summary: EmailSummary) {
        val shareText = buildString {
            appendLine("Email Summary - ${summary.TimeDate}")
            appendLine()
            append(summary.Summary)
        }
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        
        val shareIntent = Intent.createChooser(sendIntent, "Share Summary")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListenerRegistration?.remove()
    }
}
