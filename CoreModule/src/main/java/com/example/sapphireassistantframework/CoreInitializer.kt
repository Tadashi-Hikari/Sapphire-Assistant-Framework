package com.example.sapphireassistantframework

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SAFService
import java.util.*

class StartupService: SAFService() {
    private lateinit var sapphire_apps: LinkedList<Pair<String, String>>

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun scanInstalledModules(){

    }

    fun checkModuleRegistration(){

    }
}