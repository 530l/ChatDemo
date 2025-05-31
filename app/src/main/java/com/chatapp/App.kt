package com.chatapp

import android.app.Application


class App : Application() {

    var keyboardHeight = 0

    companion object {
        @JvmStatic lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}