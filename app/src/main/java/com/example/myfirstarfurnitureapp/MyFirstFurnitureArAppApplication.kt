package com.example.myfirstarfurnitureapp

import android.annotation.SuppressLint
import android.app.Application
import java.util.*

class MyFirstFurnitureArAppApplication :Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        lateinit var application: Application
        val APPLICATION by lazy { application }
    }
}