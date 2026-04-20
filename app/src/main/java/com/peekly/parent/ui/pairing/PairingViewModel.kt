package com.peekly.parent.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peekly.parent.data.PairingCodeDto
import com.peekly.parent.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PairingUiState {
    object Loading : PairingUiState()
    data class Success(val pairing: PairingCodeDto) : PairingUiState()
    data class Error(val message: String) : PairingUiState()
}

class PairingViewModel : ViewModel() {

    private val _state = MutableStateFlow<PairingUiState>(PairingUiState.Loading)
    val state: StateFlow<PairingUiState> = _state

    fun generateCode(childId: Long) {
        viewModelScope.launch {
            _state.value = PairingUiState.Loading
            try {
                val response = ApiClient.instance.generatePairingCode(childId)
                if (response.isSuccessful && response.body() != null) {
                    _state.value = PairingUiState.Success(response.body()!!)
                } else {
                    _state.value = PairingUiState.Error("Failed to generate pairing code")
                }
            } catch (e: Exception) {
                _state.value = PairingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
