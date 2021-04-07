package com.example.multiprocessmodule

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random

/*
This also can be used to wait for a response for a single intent. Perhaps it should have a different name

The biggest difference between this, and how something like the CoreRegistrationService works is that
the CoreRegistrationService is idle until the last intent returns. This one is meant to to processing
in parallel, and as such has a different kind of complexity.

I believe this may *have* to bind CoreService, now that Uris are the only way to pass around information
I can't really record it to a JSON file the same way.
 */

class MultiprocessServiceRefined: SapphireFrameworkService(){

	// This ties the Multiprocess to Core much more than I wanted. Perhaps I should specify 'levels' of modules
	inner class Connection() : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			Log.i(this.javaClass.name, "Service connected")
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i(this.javaClass.name, "Service disconnected")
		}
	}

	// This could be replaced w/ core ID
	val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
	// This holds the value while waiting for all returns
	var storedIntents = mutableListOf<Intent>()
	// This holds the relavent indexes for each ID
	var activeIntents = JSONObject()
	var connection = Connection()

	override fun onCreate(){
		var coreIntent = Intent().setClassName(this.packageName,"com.example.sapphireassistantframework.CoreService")
		bindService(coreIntent,connection,0)
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.hasExtra(MULTIPROCESS_ID)){
			true -> evaluateMultiprocessIntent(intent)
			false -> intializeMultiprocessIntent(intent)
			else -> Log.d(this.javaClass.name, "There was an intent error. Stopping Multiprocess Module...")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	// Do what must be done
	fun intializeMultiprocessIntent(intent: Intent?){
		var multiprocessIntent = Intent(intent)
		// Give it an ID for tracking
		multiprocessIntent = generateId(intent)
		// Add the initial intent and its data to storage, to await all results
		activeIntents.put(multiprocessIntent.getStringExtra(MULTIPROCESS_ID),multiprocessIntent)
		var independentRoutes = loadMultipleRoutes

	}

	fun loadMultipleRoutes(intent: Intent?){
		var preparedIntent = prepareIntent(intent!!)
		for(route in preparedIntent.getStringArrayListExtra("MULTIPROCESS_ROUTE_LIST")!!){
			var packageClass = route.split(";")
			preparedIntent.setClassName(packageClass[0],packageClass[1])
		}
	}

	// This is just a convenience method to help make things more readable
	fun prepareIntent(intent: Intent): Intent{
		var preparedIntent = Intent(intent)
		preparedIntent = regexRouteString(preparedIntent)
		preparedIntent = makeMultiprocessList(preparedIntent)
		// Make sure that every module knows what's going on
		preparedIntent.setAction(ACTION_REQUEST_FILE_DATA)
		// Know where to send it back when its done
		preparedIntent.putExtra(ROUTE,)
		return preparedIntent
	}

	// This is unique to the Multiprocess Module. I need it to look for the unique () syntax
	fun regexRouteString(intent: Intent): Intent{
		var route = intent.getStringExtra(ROUTE)!!
		// Break out the multiprocess syntax
		var start = route.indexOf("(")+1
		var end = route.indexOf(")",start)
		// Return the multiprocess block
		var multiprocessRoute = route.substring(start,end)
		// Return the rest of the information
		var remainingRoute = route.substring(end+1,)

		// I think it's just easier to pass around the intent right now
		intent.putExtra("MULTIPROCESS_ROUTE",multiprocessRoute)
		intent.putExtra(ROUTE, remainingRoute)
		return intent
	}

	// This simply takes the multiprocess route string and turns it in to a list
	fun makeMultiprocessList(intent: Intent): Intent{
		var preparedIntent = Intent(intent)
		var routeList: ArrayList<String> = preparedIntent.getStringExtra("MULTIPROCESS_ROUTE")!!.split(",") as ArrayList<String>
		preparedIntent.putExtra("MULTIPROCESS_ROUTE_LIST", routeList)
		return preparedIntent
	}

	fun generateId(intent: Intent?): Intent{
		var id = -1
		do{
			id = Random.nextInt().absoluteValue
		}while(id == -1)

		intent!!.putExtra(MULTIPROCESS_ID,id)
		return intent
	}

	fun evaluateMultiprocessIntent(intent: Intent?){

	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}