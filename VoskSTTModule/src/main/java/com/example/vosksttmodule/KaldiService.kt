package com.example.vosksttmodule


import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONObject
import org.kaldi.*
import java.io.File
import java.lang.Exception

// This is an extension of service, but it could use a MycroftSkill interface which my help developers
class KaldiService: RecognitionListener, SAFService(){

    //model should be available internally
    //private val model = ???
    // This is better as a lateinit
    private lateinit var hotwordRecognizer: KaldiRecognizer
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
        if("mega man" in json.getString("text")){
            var intent = Intent()
            intent.setClassName(this,"${this.packageName}.voiceassistant.CoreVoiceInteractionService")
            intent.setAction("android.speech.RecognitionService")
            Log.i(this.javaClass.name,"Sending to assistant....")
            //recognizer.cancel()
            //recognizer.shutdown()
            startService(intent)
            //onDestroy()

        }else if(json.getString("text") != "") {
            // This is a input module, it should always be sending to core. How would I wrap it?
            var coreServiceIntent: Intent = Intent()
            coreServiceIntent.putExtra(MESSAGE, utterance)
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
        var intent = Intent()
        var sapphireService = object: SapphireFrameworkService(){
            override fun onBind(intent: Intent?): IBinder? {
                TODO("Not yet implemented")
            }
        }
        intent.setClassName(this,"com.example.sapphireassistsantframework.voiceassistant.CoreVoiceInteractionService")
        intent.setAction(sapphireService.ACTION_SAPPHIRE_INITIALIZE)
        startService(intent)
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