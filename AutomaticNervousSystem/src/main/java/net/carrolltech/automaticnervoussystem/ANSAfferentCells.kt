package net.carrolltech.automaticnervoussystem

import android.content.Intent
import net.carrolltech.nervoussystemlibrary.NervousSystemService

class ANSAfferentCells: NervousSystemService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // Most of these can probably be moved to a superclass
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun initModule() {
        super.initModule()
        
        val intent = Intent().setClassName(this, "SystemCheckAndRegister")
        startService(intent)
    }
}