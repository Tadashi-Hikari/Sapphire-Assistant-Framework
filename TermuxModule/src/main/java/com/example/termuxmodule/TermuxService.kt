package com.example.termuxmodule

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TermuxService : Service() {

    val ARGS = "com.termux.RUN_COMMAND_ARGUMENTS"
    val WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    val PATH = "com.termux.RUN_COMMAND_PATH"
    val BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    // This should pretty much send off what I need/am looking for
    fun sendIntent(){
        var intent = Intent()
        intent.setClassName("com.termux", "com.termux.app.RunCommandService")
        intent.setAction("com.termux.RUN_COMMAND")
        intent.putExtra(PATH, "~/test.sh")
        intent.putExtra(BACKGROUND, true)
        startService(intent)
    }

    fun formatForCommandLine(){

    }
}