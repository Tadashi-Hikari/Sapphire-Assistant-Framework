package net.carrolltech.interactor

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import net.carrolltech.nervoussystemlibrary.NervousSystemService
import org.json.JSONObject

class InitializerService: NervousSystemService(){

    class connection: ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    fun initialize(){
        var config = JSONObject()

        // The key is... a module to set up. It just runs through the modules
        for(key in config.keys()){
            var module = key.split(";")

            // Key is the package;component
            var initIntent = Intent().setClassName(module[0],module[1])
            // Value is the aciton
            initIntent.setAction(config.getString(key))

            // This is the only space I need to start things this way. The rest is handled by ThalamusRelay
            bindService(initIntent,connection(),0)
            startService(initIntent)
        }
    }
}