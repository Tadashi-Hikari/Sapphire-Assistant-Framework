package com.example.sapphireassistantframework

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import edu.stanford.nlp.ie.KBPTokensregexExtractor
import java.util.*

// Should this be a long running service? Or spawn per receipt?
// Should it be an inner class of CoreService?
class CoreServicePostOffice: Service(), Runnable {

    private var pipeline: LinkedList<String> = LinkedList<String>()

    fun updateListenerHooks(){
        Log.i("CoreServicePostOffice","This is not yet implemented")
    }

    // This determines where its from and what to do with it
    fun sortPost(intent:Intent){
        var sendingModule = intent.getStringExtra("FROM")

        // I needed a dynamic way to handle data, based on the apps data needs and purpose.
        if(sendingModule == null){
            Log.e("CoreServicePostOffice","Some kind of generic data received from somewhere unknown. Ignoring")
        }else if(checkSpecialFeatureFor(sendingModule)){
            doAsTheConfigSays()
        }else{
            var found = false
            for(module in pipeline){
                if(found){
                    intent.setAction(module)
                    intent.setClassName(module,module)
                    if(module == "activity") startActivity(intent)
                    else if(module == "service") startService(intent)
                    else sendBroadcast(intent)

                // I need TO module, not the sending module. This increments it by one
                }else if(sendingModule == module){
                    found = true
                }
            }
        }
    }

    fun checkSpecialFeatureFor(sendingModule:String): Boolean{
        var configs = emptyArray<Objects>()

        for(module in configs){
            if(module as String == sendingModule){
                return true
            }
        }
        return false
    }

    // I may need some default options here, such as bind, sendToModule, schedule
    fun doAsTheConfigSays(){
        Log.i("CoreServicePostOffice","I need some logic here")
    }

    fun readConfig(){
        //pipeline = config["Pipeline"]
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}