package com.example.sapphireassistantframework

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*

private val UPDATE_UI = 1

// Maybe I should move this to its own module
class CoreCentralActivity: Activity(){
    var logFileURI: Uri? = null
    var running = false

    inner class CoreCentralActivityReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("CoreCentralActivityReceiver","Received a broadcast" )
            if(intent?.action == "UPDATE"){
                Log.i("CoreCentralActivityReceiver","Received an UPDATE broadcast" )
                var utterance = intent?.getStringExtra("HYPOTHESIS")
                if(utterance != null) {
                    updateUIFragment(utterance)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_central)

        var br: CoreCentralActivityReceiver = CoreCentralActivityReceiver()
        val filter = IntentFilter("UPDATE")
        registerReceiver(br, filter)


        checkIfFirstRun()

        // Maybe I should just move this whole logic block
        var intent: Intent? = this.intent
        if(intent != null) {
            Log.i("CoreCentralActivity", "There was an intent!")
            if (intent.action == "UPDATE") {
                Log.i("CoreCentralActivity", "There was an UPDATE action")
                var utterance = intent.getStringExtra("HYPOTHESIS")
                if (utterance != null) {
                    Log.i("CoreCentralActivity", "Utterance is ${utterance}")
                    updateUIFragment(utterance)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        var intent: Intent = Intent().setClassName(this,"com.example.sapphireassistantframework.CoreService")
        intent.setAction("VISIBLE")
        startService(intent)
    }

    override fun onPause() {
        super.onPause()
        var intent: Intent = Intent().setClassName(this,"com.example.sapphireassistantframework.CoreService")
        intent.setAction("HIDDEN")
        startService(intent)
    }

    fun CoreServiceManager(view: View){
        if(running){
            stopCoreService()
            running = false
        }else{
            startCoreService()
            running = true
        }
    }

    fun startCoreService(){
        var intent: Intent = Intent().setClassName(this,"com.example.sapphireassistantframework.CoreService")
        intent.setAction("sapphire_assistant_framework.BIND")
        startService(intent)
        intent.setAction("VISIBLE")
        startService(intent)
    }

    fun stopCoreService(){
        var intent: Intent = Intent()
        intent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
        intent.putExtra("LOG_FILE_URI",logFileURI)
        stopService(intent)
    }

    fun updateUIFragment(utterance: String){
        val text_box: TextView = findViewById(R.id.utteranceText)
        text_box.setText(utterance)
    }

    fun checkIfFirstRun(){
        var UNIQUE_REQUEST_CODE = 1
        val preferences = getPreferences(Context.MODE_PRIVATE)
        val firstRun = preferences.getBoolean("FIRST_RUN", false)
        val logFileUri: Uri? = null

        if(firstRun){
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, UNIQUE_REQUEST_CODE)
                preferences.edit().putBoolean("FIRST_RUN", false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_CANCELED){
            Log.e("CoreCentralActivity","An StartActivityForResult was cancelled")
            return
        }else if(resultCode == 1){
            // get the URI here
            if(intent == null){
                Log.e("CoreCentralActivity","There was no intent in the StartActivityForResult")
            }else{
                intent.data.also { uri ->
                    logFileURI = uri
                }
            }
        }else{
            Log.e("CoreCentralActivity","An unkown StartActivityForResult was received")
            return
        }
    }
}