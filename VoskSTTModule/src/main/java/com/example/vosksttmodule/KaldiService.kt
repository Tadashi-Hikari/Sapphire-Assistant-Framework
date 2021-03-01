package com.example.vosksttmodule


import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import org.json.JSONObject
import org.kaldi.*
import java.io.File
import java.lang.Exception

// This is an extension of service, but it could use a MycroftSkill interface which my help developers
class KaldiService: RecognitionListener, SAFService(){

    //model should be available internally
    //private val model = ???
    // This is better as a lateinit
    private lateinit var recognizer: CustomSpeechRecognizer
    private lateinit var startupIntent: Intent

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("kaldi_jni");

        // This is going to make it run through the process twice. I need to offload the creation
        // I actually think I need to move setup to onBind()
        setup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(this.javaClass.name,"KaldiService intent received")
        // Make sure it has Env set. This might be movable to SAFService, and should DEF be error checked
        if((startupIntent == null) or (startupIntent.action == ACTION_SAPPHIRE_UPDATE_ENV)){
            startupIntent = intent!!
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i("KaldiService","kaldi service started")
        var binder: IBinder = Binder()

        return binder
    }

    // Maybe this should be a broadcast
    fun sendUtterance(utterance: String){
        var json = JSONObject(utterance)
        if(json.getString("text") != "") {
            // This needs to not be hardcoded... How can a skill know? I need to pass it the core details
            // I think I either need to set meta-data or resources
            var coreServiceIntent: Intent = Intent()

            //var coreModule = getCoreModule(startupIntent)
            //var packageClass = coreModule.split(";")


            //var packageName = packageClass[0]; var className = packageClass[1]
            var packageName = "com.example.sapphireassistantframework"
            var className = "${packageName}.CoreService"

            // This is setting it to go explicitly to CORE... which means I need to change SAFService
            coreServiceIntent.setClassName(packageName,className)
            coreServiceIntent.putExtra(MESSAGE, utterance)
            // This was just change from postage (which now holds env var) to route... It's saying to take KaldiServices default route. Do I need to change the behavior?
            coreServiceIntent.putExtra(ROUTE,"com.example.vosksttmodule.KaldiService")
            Log.i("KaldiService", "Utterance hypothesis dispatched")
            startSAFService(coreServiceIntent)
        }
    }

    fun setup(){
        var startTime  = System.currentTimeMillis()
        var result = StringBuilder()

        var assets = Assets(this)
        Log.i("KaldiService","Created the assets object")
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        // These need to be moved out of setup, into their own thread
        var model = Model(assetDir.toString()+"/model-android")
        // This is the recognizer itself

        // See if I need to change this with a kaldi recognizer
        recognizer = CustomSpeechRecognizer(model)
        recognizer.addListener(this)
        recognizer.startListening()
    }

    override fun onError(p0: Exception?) {
        Log.e("KaldiService", "Kaldi ran into an error")
        // fix the error, or tell the user
    }

    // This will pass a result while running, No need to start or stop the recognizer
    // I can subclass this object as a hotword listener if I need to.
    override fun onPartialResult(p0: String?) {
        // scan this for in between stuff
    }

    // This will pass a result while running, No need to start or stop the recognizer
    override fun onResult(hypothesis: String) {
        // print/pass the output, restart the loop

        Log.i("KaldiService","Result: ${hypothesis}")
        // This sends the utterance to CoreService
        sendUtterance(hypothesis)
    }

    override fun onTimeout() {
    }

    override fun onDestroy() {
        super.onDestroy()

        recognizer.cancel()
        recognizer.shutdown()
    }
}