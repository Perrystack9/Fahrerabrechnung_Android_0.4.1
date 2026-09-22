package de.fahrerabrechnung.core.model

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Content<T>(val data: T) : UiState<T>
    data class Empty(val message: String) : UiState<Nothing>
    data class Partial<T>(val data: T, val message: String) : UiState<T>
    data class Unclear(val message: String) : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}
