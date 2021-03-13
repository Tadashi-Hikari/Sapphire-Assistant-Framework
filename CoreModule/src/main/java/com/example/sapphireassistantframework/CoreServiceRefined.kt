package com.example.sapphireassistantframework

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.NervousSystemService
import org.json.JSONArray
import java.lang.Exception

/*
This exists somewhere between the brain and body. This movement to me, is akain to the nervous system
and sending information from local spinal column/muscular reflexes, to more complex thinking tasks.
This would be indicitive of the nervous system as a whole
 */

class CoreServiceRefined: NervousSystemService(){
	//State variables
	var conscious = false

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}

	override fun onStartCommand(signal: Intent?, flags: Int, startId: Int): Int {
		if(isValid(signal)){
			handleSignal(signal!!)
		}else{
			Log.i(this.javaClass.name,"Just brain noise...")
		}
		// This may need to be moved, if I am to do things in the background
		return super.onStartCommand(signal, flags, startId)
	}

	// Check if it exists, and has the minimum information needed to go further
	fun isValid(signal: Intent?): Boolean{
		try{
			return signal!!.hasExtra(FROM)
		}catch(exception: Exception){
			return false
		}
	}

	// What is the nervous systems function called
	fun handleSignal(signal: Intent){
		// Looking for a better mental abstraction. These actions are more akin to heartbeats, digestion, etc. Autonomous actions, but unchangeable
		var autonomous = signal.action
		// Handle actions here
		when(conscious){
			true ->	when(autonomous) {
				ACTION_SAPPHIRE_CORE_BIND -> bind(signal)
				ACTION_SAPPHIRE_MODULE_REGISTER -> propagateSignal(signal)
				ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> wake()
				else -> propagateSignal(signal)
			}
			false -> when(autonomous){
				ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
				ACTION_SAPPHIRE_CORE_REQUEST_DATA -> propagateSignal(signal)
			}
		}
	}




	fun propagateSignal(signal: Intent){
		when(signal.hasExtra(ROUTE)){
			true -> continueAlongCircuit(signal)
			false -> determineNeuralCircuit(signal)
		}
	}

	fun continueAlongCircuit(actionPotential: Intent){
		var circuitPath = JSONArray(actionPotential.getStringExtra(ROUTE))
		var circuit = circuitPath.getString(0).split(";")

		var synapseIntent = Intent()
		synapseIntent.setClassName(circuit.component1(),circuit.component2())
		synapse(synapseIntent)
	}

	// I don't like the word reflex here. It's misleading... It's just determining the propagation route
	fun determineNeuralCircuit(signal: Intent){


		initialSynapse(synapseIntent)
	}

	fun bind(signal: Intent){
	}

	/*
	 a state between non-consiousness and consciousness. I think this will change when I introduce
	 sleep-based data processing (when charging)
	 */
	fun wake(){
	}

	// Run through the registration process
	fun startRegistrationService(){
		var registrationIntent = Intent().setClassName(this.packageName,"${this.packageName}.CoreRegistrationService")
		registrationIntent.setAction(ACTION_SAPPHIRE_INITIALIZE)
		Log.v(this.javaClass.name,"starting service ${"${this.packageName}.CoreRegistrationService"}")
		startService(registrationIntent)
	}

}