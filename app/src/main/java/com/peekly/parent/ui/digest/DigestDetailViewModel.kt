package com.peekly.parent.ui.digest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peekly.parent.data.DigestDto
import com.peekly.parent.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DigestUiState {
    object Loading : DigestUiState()
    data class Success(val digests: List<DigestDto>) : DigestUiState()
    data class Error(val message: String) : DigestUiState()
}

class DigestDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow<DigestUiState>(DigestUiState.Loading)
    val state: StateFlow<DigestUiState> = _state

    fun load(childId: Long) {
        viewModelScope.launch {
            _state.value = DigestUiState.Loading
            try {
                val response = ApiClient.instance.getDigestHistory(childId)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    _state.value = DigestUiState.Success(response.body()!!)
                } else {
                    _state.value = DigestUiState.Error("No digest found for this child")
                }
            } catch (e: Exception) {
                _state.value = DigestUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
