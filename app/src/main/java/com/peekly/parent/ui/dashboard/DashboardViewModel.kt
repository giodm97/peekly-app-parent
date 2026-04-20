package com.peekly.parent.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peekly.parent.data.ChildDto
import com.peekly.parent.data.DigestDto
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChildSummary(
    val child: ChildDto,
    val latestDigest: DigestDto?
)

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val children: List<ChildSummary>) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state

    private var parentSub: String = ""

    init {
        viewModelScope.launch {
            parentSub = ParentDataStore.getOrCreateParentSub(app)
            loadChildren()
        }
    }

    fun refresh() {
        viewModelScope.launch { loadChildren() }
    }

    private suspend fun loadChildren() {
        _state.value = DashboardUiState.Loading
        try {
            val response = ApiClient.instance.getChildren(parentSub)
            if (!response.isSuccessful || response.body() == null) {
                _state.value = DashboardUiState.Error("Failed to load children")
                return
            }
            val children = response.body()!!
            val summaries = coroutineScope {
                children.map { child ->
                    async {
                        val digest = runCatching {
                            ApiClient.instance.getDigest(child.id).body()
                        }.getOrNull()
                        ChildSummary(child, digest)
                    }
                }.awaitAll()
            }

            _state.value = DashboardUiState.Success(summaries)
        } catch (e: Exception) {
            _state.value = DashboardUiState.Error(e.message ?: "Unknown error")
        }
    }
}
