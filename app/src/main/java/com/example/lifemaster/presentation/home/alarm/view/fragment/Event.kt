package com.example.lifemaster.presentation.home.alarm.view.fragment

class Event<out T>(private val data: T) {
    private var isHandled = false

    fun getDataIfNotHandled(): T? {
        if(isHandled) return null else {
            isHandled = true
            return data
        }
    }
}