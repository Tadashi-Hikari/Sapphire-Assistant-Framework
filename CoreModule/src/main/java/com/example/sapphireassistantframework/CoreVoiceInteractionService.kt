package com.example.sapphireassistantframework

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

class CoreVoiceInteractionService: VoiceInteractionService(){
	inner class currentService: VoiceInteractionSessionService(){
		override fun onNewSession(args: Bundle?): VoiceInteractionSession {
			TODO("Not yet implemented")
		}
	}

	override fun onCreate() {
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.action){
			"START_NEW_SESSION" -> currentService()
			"START_HOTWORD_LISTENER" -> bindSTT()
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onLaunchVoiceAssistFromKeyguard() {
		super.onLaunchVoiceAssistFromKeyguard()
		currentService()
	}

	override fun showSession(args: Bundle?, flags: Int) {
		super.showSession(args, flags)
		var activityIntent = Intent()
		startActivity(activityIntent)
	}

	fun bindSTT(){
		var voskIntent = Intent()
		//bindService(voskIntent)
	}


}