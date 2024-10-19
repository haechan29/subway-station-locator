package com.hc.subway_station_locator.presentation.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class BaseViewModel: ViewModel() {
    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()

    protected fun <S: State<T>, T> setState(mutableStateFlow: MutableStateFlow<T>, reduce: T.() -> T) {
        mutableStateFlow.update { oldValue ->
            oldValue.reduce().also { newValue ->
                if (newValue is State.Fail) setEffect { MainEffect.ShowToast(newValue.string) }
            }
        }
    }

    protected fun setEffect(builder: () -> Effect) {
        viewModelScope.launch {
            _effect.send(builder())
        }
    }
}