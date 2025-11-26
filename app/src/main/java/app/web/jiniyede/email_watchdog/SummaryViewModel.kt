package app.web.jiniyede.email_watchdog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SummaryViewModel: ViewModel() {
    private val _summaries = MutableStateFlow<List<EmailSummary>>(emptyList())
    val summaries: StateFlow<List<EmailSummary>> = _summaries.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    init {
        loadSummaries()
    }

    private fun loadSummaries() {
        db.collection("emails_summaries")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("SummaryViewModel", "listen:error", e)
                    return@addSnapshotListener
                }

                val summaryList = mutableListOf<EmailSummary>()
                for (doc in snapshots!!) {
                    doc.toObject(EmailSummary::class.java).let { summary ->
                        summaryList.add(summary.copy(id = doc.id))
                    }
                }
                _summaries.value = summaryList
            }
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
                .addOnSuccessListener { Log.d("SummaryViewModel", "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w("SummaryViewModel", "Error deleting document", e) }
        }
    }

    fun shareSummary(summary: EmailSummary) {
        // TODO: Implement share functionality
        // Could use ShareSheet or create a text export
    }
}
