package com.example.sapphireassistantframework.voiceassistant

import android.app.PendingIntent.getService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import android.widget.Toast
import com.example.componentframework.SapphireFrameworkService
import com.example.sapphireassistantframework.CoreService

/*
This class is the primary, long running service for the VoiceInteractionApplication.
I could bind this to CoreService and move it to Kaldi but then Kaldi would need to declare the
assistant features in its manifest, making Kaldis replacement harder.
 */

class CoreVoiceInteractionService: VoiceInteractionService(){
	inner class CoreVoiceInteractionSessionService: VoiceInteractionSessionService(){
		override fun onNewSession(args: Bundle?): CoreVoiceInteractionSession{
			return CoreVoiceInteractionSession(this)
		}
	}

	val voiceSession = CoreVoiceInteractionSessionService()
	val sapphireSerivce = object: SapphireFrameworkService(){
		override fun onBind(intent: Intent?): IBinder? {
			//Doesn't do shit
			return null
		}
	}


	lateinit var session: CoreVoiceInteractionSession
	override fun onCreate() {
		super.onCreate()
		Log.i(this.javaClass.name,"Assistant created")
		startVoskHotwordDetector()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		// These won't run, cause it needs the SPECIAL BIND ACTION
		if(intent?.action == sapphireSerivce.ACTION_SAPPHIRE_INITIALIZE){
			// This is
			Toast.makeText(this,"Speech recognition starting",Toast.LENGTH_SHORT)
		}else if(intent?.action == "android.speech.RecognitionService"){
			// I don't think this ever runs, because it's caught up in the same thread
			Log.i(this.javaClass.name,"Hotword detected")
			onVoskHotwordDetection()
		}
		return super.onStartCommand(intent, flags, startId)
	}

	fun startVoskHotwordDetector(){
		var hotwordIntent = Intent()
		hotwordIntent.setClassName(this.packageName,"com.example.vosksttmodule.KaldiService")
		hotwordIntent.setAction(sapphireSerivce.ACTION_SAPPHIRE_INITIALIZE)
		var pendingHotword = getService(this,0,hotwordIntent, 0)
		pendingHotword.send()
		// Start the service within this process, to give it audio access
		startService(hotwordIntent)
		var connection = Connection()
		// Bind the service so it doesn't die
		bindService(hotwordIntent, connection, Context.BIND_AUTO_CREATE)
	}

	inner class Connection() : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			Log.i(this.javaClass.name, "Service connected")
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i(this.javaClass.name, "Service disconnected")
		}
	}

	fun onVoskHotwordDetection(){
		//Stop Hotword detector
		//Listen for command hotwords parallel
		session = voiceSession.onNewSession(null)
	}

	override fun onReady() {
		super.onReady()
	}

	override fun onShutdown() {
		super.onShutdown()
	}
}