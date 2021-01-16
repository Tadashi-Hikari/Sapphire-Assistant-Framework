package com.example.sapphireassistantframework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.componentframework.SAFService
import org.json.JSONObject
import org.kaldi.*
import java.io.File
import java.lang.Exception

// This is an extension of service, but it could use a MycroftSkill interface which my help developers
class CoreKaldiService: RecognitionListener, SAFService(){

    //model should be available internally
    //private val model = ???
    // This is better as a lateinit
    private lateinit var recognizer: CoreCustomSpeechRecognizer
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("kaldi_jni");
        // This is going to make it run through the process twice. I need to offload the creation
        buildForegroundNotification()
        setup()
    }

    fun buildForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel("SOME FUCKING ID", "SOME FUCKING NAME", importance).apply {
                    description = "A fucking annoying test"
                }

            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // This is the notification for the foreground service. Maybe have it lead into other bound services
        var builder = NotificationCompat.Builder(this, "SOME FUCKING ID")
            .setSmallIcon(R.drawable.assistant)
            .setContentTitle("A FUCKING TITLE")
            .setContentText("THE FUKCING THING IS RUNNING")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            // I defined notification id as 1, may need to change this later
            notify(1337, builder.build())
        }
        startForeground(1337,builder.build())
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i("CoreKaldiService","kaldi service started")
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
            coreServiceIntent.setClassName(
                "com.example.sapphireassistantframework",
                "com.example.sapphireassistantframework.CoreService"
            )
            coreServiceIntent.putExtra(MESSAGE, utterance)
            coreServiceIntent.putExtra(POSTAGE,"com.example.sapphireassistantframework.CoreKaldiService")
            Log.i("CoreKaldiService", "Utterance hypothesis dispatched")
            startService(coreServiceIntent)
        }
    }

    fun setup(){
        var startTime  = System.currentTimeMillis()
        var result = StringBuilder()

        var assets = Assets(this)
        Log.i("CoreKaldiService","Created the assets object")
        var assetDir: File = assets.syncAssets()

        Vosk.SetLogLevel(0)

        // These need to be moved out of setup, into their own thread
        var model = Model(assetDir.toString()+"/model-android")
        // This is the recognizer itself

        // See if I need to change this with a kaldi recognizer
        recognizer = CoreCustomSpeechRecognizer(model)
        recognizer.addListener(this)
        recognizer.startListening()
    }

    override fun onError(p0: Exception?) {
        Log.e("CoreKaldiService", "Kaldi ran into an error")
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

        Log.i("CoreKaldiService","Result: ${hypothesis}")
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