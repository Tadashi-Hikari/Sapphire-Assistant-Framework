package net.carrolltech.nervoussystemlibrary

import android.content.Intent

abstract class StatefulService: NervousSystemService(){

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_SAPPHIRE_STATE_CREATE -> createState()
            ACTION_SAPPHIRE_STATE_READ -> readState()
            ACTION_SAPPHIRE_STATE_UPDATE -> updateState()
            ACTION_SAPPHIRE_STATE_DESTROY -> destroyState()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // These can probably be moved to a superclass
    open fun createState(){
        //LoadState
    }

    open fun readState(){
        // Return state
    }

    open fun updateState(){

    }

    open fun destroyState(){

    }
}