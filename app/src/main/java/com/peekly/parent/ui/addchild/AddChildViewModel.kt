package com.peekly.parent.ui.addchild

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peekly.parent.data.CreateChildRequest
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddChildUiState {
    object Idle : AddChildUiState()
    object Loading : AddChildUiState()
    data class Success(val childId: Long) : AddChildUiState()
    data class Error(val message: String) : AddChildUiState()
}

class AddChildViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<AddChildUiState>(AddChildUiState.Idle)
    val state: StateFlow<AddChildUiState> = _state

    fun createChild(name: String, age: Int) {
        viewModelScope.launch {
            _state.value = AddChildUiState.Loading
            try {
                val parentSub = ParentDataStore.getOrCreateParentSub(getApplication())
                val response = ApiClient.instance.createChild(
                    CreateChildRequest(name = name, age = age, parentSub = parentSub)
                )
                if (response.isSuccessful && response.body() != null) {
                    _state.value = AddChildUiState.Success(response.body()!!.id)
                } else {
                    _state.value = AddChildUiState.Error("Failed to create child. Try again.")
                }
            } catch (e: Exception) {
                _state.value = AddChildUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _state.value = AddChildUiState.Idle
    }
}
