package net.carrolltech.nervoussystemlibrary

import android.app.Service
import android.content.Intent
import android.os.IBinder

abstract class NervousSystemService: Service(){
    // Standard Universal API actions
    val ACTION_SAPPHIRE_INITIALIZE = "assistant.framework.action.INITIALIZE"
    val ACTION_SAPPHIRE_SHUTDOWN = "assistant.framework.action.SHUTDOWN"
    // Required Stateful Module API actions
    val ACTION_SAPPHIRE_STATE_CREATE = "assistant.framework.action.STATE_CREATE"
    val ACTION_SAPPHIRE_STATE_READ = "assistant.framework.action.STATE_READ"
    val ACTION_SAPPHIRE_STATE_UPDATE = "assistant.framework.action.STATE_UPDATE"
    val ACTION_SAPPHIRE_STATE_DESTROY = "assistant.framework.action.STATE_DESTROY"
    // Required ThalamusRelay (Core) Module API actions
    val ACTION_SAPPHIRE_CORE_STOP = "assistant.framework.action.CORE_STOP"
    val ACTION_SAPPHIRE_CORE_START = "assistant.framework.action.START" // Might be unneeded
    val ACTION_SAPPHIRE_CORE_BLOCK = "assistant.framework.action.BLOCK"
    val ACTION_SAPPHIRE_CORE_PASSTHROUGH = "assistant.framework.action.PASSTHROUGH" // Might be unneeded
    // Required PerceptionProcessor (Processor) API actions
    val ACTION_SAPPHIRE_PROCESS = "assistant.framework.action.PROCESS"
    val ACTION_SAPPHIRE_TRAIN = "assistant.framework.action.TRAIN"
    // Required LongTermMemory (FileProvider) API actions
    val ACTION_SAPPHIRE_READ = "assistant.framework.action.READ"
    val ACTION_SAPPHIRE_WRITE = "assistant.framework.action.WRITE"
    val ACTION_SAPPHIRE_UPDATE = "assistant.framework.action.UPDATE" // Might be unneeded
    val ACTION_SAPPHIRE_DELETE = "assistant.framework.action.DELETE"
    // Required ANS (Multiprocess) API actions
    val ACTION_SAPPHIRE_DISPATCH = "assistant.framework.action.DISPATCH"
    val ACTION_SAPPHIRE_MULTIPLEX = "assistant.framework.action.MULTIPLEX"
    // Required Intent extras
    val SAPPHIRE_EXTRA_TO = "assistant.framework.extra.TO"
    val SAPPHIRE_EXTRA_FROM = "assistant.framework.extra.FROM"
    val SAPPHIRE_EXTRA_MESSAGE = "assistant.framework.extra.MESSAGE"
    val SAPPHIRE_EXTRA_ROUTE = "assistant.framework.extra.ROUTE"
    val SAPPHIRE_EXTRA_CORE = "assistant.framework.extra.CORE"
    val SAPPHIRE_EXTRA_KEYS = "assistant.framework.extra.KEYS"
    // Expected existing modules
    val SAPPHIRE_CORE_MODULE = "assistant.framework.module.CORE"
    val SAPPHIRE_PROCESS_MODULE = "assistant.framework.module.PROCESS"
    val SAPPHIRE_MULTIPROCESS_MODULE = "assistant.framework.module.MULTIPROCESS"
    val SAPPHIRE_STORAGE_MODULE = "assistant.framework.module.STORAGE"

    //Convenience variables
    var CLASS = this.javaClass.name
    var PACKAGE = this.packageName
    var Log = LogOverride()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_SAPPHIRE_INITIALIZE -> initModule()
            ACTION_SAPPHIRE_SHUTDOWN -> initModule()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    open fun initModule(){

    }

    open fun shutdownModule(){

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    inner class LogOverride{
        fun i(message: String){
            android.util.Log.i(CLASS,message)
        }

        fun d(message: String){
            android.util.Log.d(CLASS,message)
        }

        fun e(message: String){
            android.util.Log.e(CLASS,message)
        }

        fun v(message: String){
            android.util.Log.v(CLASS,message)
        }

        fun w(message: String){
            android.util.Log.w(CLASS,message)
        }
    }
}