package com.example.loginapp.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 基础 ViewModel - 提供状态管理和事件处理框架
 */
abstract class BaseViewModel<State : UiState, Event : UiEvent, Effect : UiEffect> : ViewModel() {

    // UI 状态
    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<State> = _state.asStateFlow()

    // 一次性事件（导航、Toast、Snackbar 等）
    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    protected val currentState: State
        get() = _state.value

    abstract fun initialState(): State

    /**
     * 处理 UI 事件
     */
    abstract fun onEvent(event: Event)

    /**
     * 更新状态
     */
    protected fun updateState(reduce: State.() -> State) {
        _state.value = currentState.reduce()
    }

    /**
     * 发送一次性事件
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}

interface UiState
interface UiEvent
interface UiEffect
