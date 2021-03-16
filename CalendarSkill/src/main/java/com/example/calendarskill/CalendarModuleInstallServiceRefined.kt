package com.example.calendarskill

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkRegistrationService
import com.example.componentframework.SapphireFrameworkService

class CalendarModuleInstallServiceRefined: SapphireFrameworkRegistrationService(){

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.action){
			ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent!!)
			ACTION_SAPPHIRE_DATA_TRANSFER -> transferData()
		}
		return super.onStartCommand(intent, flags, startId)
	}

	fun transferData(){

	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}