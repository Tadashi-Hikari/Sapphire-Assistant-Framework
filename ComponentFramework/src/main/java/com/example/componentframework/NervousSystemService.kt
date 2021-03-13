package com.example.componentframework

import android.app.Service
import android.content.Intent
import android.os.IBinder

abstract class NervousSystemService: Service() {
	inner class LogOverride{
		fun i(name: String, message: String){
			android.util.Log.i(name,message)
			broadcastStatus(name,message)
		}

		fun d(name: String, message: String){
			android.util.Log.d(name,message)
			broadcastStatus(name,message)
		}

		fun e(name: String, message: String){
			android.util.Log.e(name,message)
			broadcastStatus(name,message)
		}

		fun v(name: String, message: String){
			android.util.Log.v(name,message)
			broadcastStatus(name,message)
		}

		fun w(name: String, message: String){
			android.util.Log.w(name,message)
			broadcastStatus(name,message)
		}
	}

	var Log = LogOverride()

	val MESSAGE="assistant.framework.protocol.MESSAGE"
	val STDERR="assistant.framework.protocol.STDERR"
	// This is going to be for ENV_VARIABLES
	val POSTAGE="assistant.framework.protocol.POSTAGE"
	val ROUTE="assistant.framework.protocol.ROUTE"
	val FROM= "assistant.framework.protocol.FROM"
	val ID = "assistant.framework.module.protocol.ID"

	// Maybe this should be used elsewhere...
	var STARTUP_ROUTE = "assistant.framework.protocol.STARTUP_ROUTE"

	val MODULE_PACKAGE = "assistant.framework.module.protocol.PACKAGE"
	val MODULE_CLASS = "assitant.framework.module.protocol.CLASS"
	var MODULE_TYPE = "assistant.framework.module.protocol.TYPE"
	val MODULE_VERSION = "assistant.framework.module.protocol.VERSION"

	/**
	 * I don't know that I need to list all of these explicitly, and I think I'll
	 * let the user override them anyway. This is just for initial install purposes
	 */
	val CORE="assistant.framework.module.type.CORE"
	val PROCESSOR="assistant.framework.module.type.PROCESSOR"
	val MULTIPROCESS="assistant.framework.module.type.MULTIPROCESS"
	// These are the ones I don't think are essential
	val INPUT="assistant.framework.module.type.INPUT"
	val TERMINAL="assistant.framework.module.type.TERMINAL"
	val GENERIC="assistant.framework.module.type.GENERIC"

	// Module specific extras
	val PROCESSOR_ENGINE="assistant.framework.processor.protocol.ENGINE"
	val PROCESSOR_VERSION="assistant.framework.processor.protocol.VERSION"
	val DATA_KEYS="assistant.framework.module.protocol.DATA_KEYS"

	// Actions
	val ACTION_SAPPHIRE_CORE_BIND="assistant.framework.core.action.BIND"
	// This is sent to the CORE from the module, so the core can handle the registration process
	// This is for a module to request *all* data from the core (implicit intent style)
	val ACTION_SAPPHIRE_ROUTE_LOOKUP = "assistant.framework.core.action.ROUTE_LOOKUP"
	val ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE = "assistant.framework.core.action.REGISTRATION_COMPLETE"
	val ACTION_SAPPHIRE_CORE_REQUEST_DATA="assistant.framework.core.action.REQUEST_DATA"
	val ACTION_SAPPHIRE_UPDATE_ENV = "action.framework.module.action.UPDATE_ENV"
	val ACTION_SAPPHIRE_MODULE_REGISTER = "assistant.framework.module.action.REGISTER"
	// this is -V on the command line
	val ACTION_SAPPHIRE_MODULE_VERSION = "assistant.framework.module.action.VERSION"
	val ACTION_SAPPHIRE_EXPORT_CONFIG = "assistant.framework.module.action.EXPORT_CONFIG"
	// This is for core to request data from a specific module
	val ACTION_SAPPHIRE_TRAIN="assistant.framework.processor.action.TRAIN"
	val ACTION_SAPPHIRE_INITIALIZE="assistant.framework.processor.action.INITIALIZE"

	val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return super.onStartCommand(intent, flags, startId)
	}

	fun broadcastStatus(name: String, message:String) {
		var notifyIntent = Intent()
		notifyIntent.putExtra(MESSAGE, "${name}: ${message}")
		notifyIntent.setAction(GUI_BROADCAST)
		sendBroadcast(notifyIntent)
	}

	fun initialSynapse(signal: Intent){
		var updatedSignal = Intent(signal)
		updatedSignal = updatePostage(signal)

		synapse(signal)
	}

	fun synapse(signal: Intent){
		var updatedSignal = Intent(signal)
		updatedSignal = updatePostage(signal)
		startService(updatedSignal)
	}

	fun updatePostage(signal: Intent): Intent{

	}
}