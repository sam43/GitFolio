package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor() : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            if (_isDarkTheme.value != isDark) {
                _isDarkTheme.value = isDark
            }
        }
    }
    fun toggleTheme() {
        viewModelScope.launch {
            _isDarkTheme.value = !_isDarkTheme.value
        }
    }
}