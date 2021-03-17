package com.example.sapphireassistantframework.voiceassistant

import android.content.Context
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.widget.Toast

/*
As far as I can tell, this is supposed to contain UI things for the assistant
 */

class CoreVoiceInteractionSession(context: Context): VoiceInteractionSession(context){
	override fun onCreate() {
		super.onCreate()
		Toast.makeText(context,"test",Toast.LENGTH_LONG).show()
		Log.i(this.javaClass.name,"Session started")
	}
}