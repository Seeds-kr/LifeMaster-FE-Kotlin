package com.example.lifemaster.presentation.home.alarm.model

sealed class DataResource<out T> {
    class Success<T>(val data: T): DataResource<T>()
    class Error(val throwable: Throwable): DataResource<Nothing>()
    object Idle: DataResource<Nothing>()
    object Loading: DataResource<Nothing>()
}