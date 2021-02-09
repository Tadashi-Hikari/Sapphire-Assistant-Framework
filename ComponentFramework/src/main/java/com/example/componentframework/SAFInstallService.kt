package com.example.componentframework

import android.content.Intent

abstract class SAFInstallService: SAFService(){

	/**
	 * Do these need to be their own modules? I don't think it's saving any time,
	 * that said, it could be saving complexity since the dev doesn't need to know
	 * extra strings to populate it
	 */

	fun registerVersion(intent: Intent, version: String): Intent{
		intent.putExtra(MODULE_VERSION,version)
		return intent
	}

	// I could overload this to take a list, or handle repeated individual calls
	// would that make it overly complex?
	fun registerType(intent: Intent, type: String): Intent{
		// I think a simple , DSV will work fine
		intent.putExtra(MODULE_TYPE, type)
		return intent
	}

	fun registerSettings(intent: Intent, component: String): Intent{
		intent.putExtra(MODULE_TYPE, component)
		return intent
	}

	// This is called for retrieveData as well
	// This one WILL save time
	fun registerData(intent: Intent, filenames: List<String>): Intent {
		return intent
	}

	fun registerModule(intent: Intent){
		// This needs to not be hardcoded. I can get the info from POSTAGE
		intent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
		startService(intent)
	}
}