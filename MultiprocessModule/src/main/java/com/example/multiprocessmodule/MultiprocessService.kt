package com.example.multiprocessmodule

import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONObject
import java.lang.Exception
import kotlin.math.absoluteValue
import kotlin.random.Random

/*
This also can be used to wait for a response for a single intent. Perhaps it should have a different name

The biggest difference between this, and how something like the CoreRegistrationService works is that
the CoreRegistrationService is idle until the last intent returns. This one is meant to to processing
in parallel, and as such has a different kind of complexity.

I believe this may *have* to bind CoreService, now that Uris are the only way to pass around information
I can't really record it to a JSON file the same way.

I need to set a variable that is user adjustable, so that this doesn't accidentally eat up system resources
 */

class MultiprocessService: SapphireFrameworkService(){

	// This ties the Multiprocess to Core much more than I wanted. Perhaps I should specify 'levels' of modules
	inner class Connection() : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			Log.i("Service connected")
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i("Service disconnected")
		}
	}

	// This could be replaced w/ core ID
	val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
	// This holds the value while waiting for all returns
	var storedIntents = mutableListOf<Intent>()
	// This holds the relavent indexes for each ID
	var intentLedger = JSONObject()
	// This holds specific information about each multiprocess intent
	var connection = Connection()

	override fun onCreate(){
		var coreIntent = Intent().setClassName(this.packageName,"com.example.sapphireassistantframework.CoreService")
		bindService(coreIntent,connection,0)
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try{
			when (intent?.hasExtra(MULTIPROCESS_ID)){
				true -> evaluateReturningIntent(intent)
				false -> handleNewMultiprocessIntent(intent)
				else -> Log.d("There was an intent error. Stopping Multiprocess Module...")
			}
		}catch(exception: Exception){
			exception.printStackTrace()
		}
		return super.onStartCommand(intent, flags, startId)
	}

	// Do what must be done
	fun handleNewMultiprocessIntent(intent: Intent?){
		// I need another nested if to handle w/ a multiprocess may have no modification
		if(intent!!.hasExtra("CUSTOM_MULTIPROCESS")){
			// This is definitely duplicate
			var multiprocessIntent = prepareIntent(intent!!)
			var customIntent = Intent(intent)
			// This holds a subJSON for each intent
			var customLedger = JSONObject(intent.getStringExtra("CUSTOM_MULTIPROCESS"))
			Log.d("customJSON = ${customLedger}")
			// I need blank to REMOVE things from here, so that I can retain the URI permissions

			// Copied from below
			storedIntents.add(multiprocessIntent)
			var intentRecord = JSONObject()
			intentRecord.put("ORIGINAL", storedIntents.size)

			// This was added in the processing
			for(process in multiprocessIntent.getStringArrayListExtra("MULTIPROCESS_ROUTE_LIST")!!){
				// If there is a custom setting for this module/intent
				if(customLedger.has(process)){
					var processSettingsJSON = customLedger.getJSONObject(process)
					// Blank out the intent, or nah. I can leave this here, cause it can be set globally
					when(customLedger.has("BLANK")) {
						// Blank it out before proceeding
						true -> {
							// I don't think this should literally be blank
							customIntent = Intent()
						}
						// Use the std intent
						false -> {
							customIntent = multiprocessIntent
						}
					}
					// Handle special flags, and just copy unk extras
					for(key in processSettingsJSON.keys()){
						// When the key is a special key, do something, else default
						Log.d("Checking key ${key}")
						when(key){
							// Set the action for the intent
							"ACTION" -> {customIntent.action = processSettingsJSON.getString(key)
								Log.d(customIntent.action!!)}
							// This inject the route in before returning to multiprocess.
							"ROUTE" -> customIntent.putExtra(ROUTE,processSettingsJSON.getString(ROUTE))
							// Convert the keys to something useable by the next intent
							"MODULE" -> customIntent.putExtra("TO",processSettingsJSON.getString("MODULE"))
							DATA_KEYS -> {
								// This is the format needed for Android
								var data_keys = mutableListOf<String>()
								// These area all of the filenames
								var jsonDataKeys = processSettingsJSON.getJSONArray(DATA_KEYS)
								Log.d("DATA_KEYS: ${jsonDataKeys}")
								// This is the index of the corrisponding uri in ClipData, or Data
								var jsonDataClipIndex = processSettingsJSON.getJSONObject("DATA_CLIP")
								// Can't forget to convert length to index
								for(index in 0..jsonDataKeys.length()-1){
									// This should be the filename
									data_keys.add(jsonDataKeys.getString(index))
									// When JSONDataKeys has
									when(jsonDataKeys.getString(index)){
										// Why did I pick negative one?...
										"-1" -> {
											Log.v("Copying Uri for ${intent.data.toString()}")
											customIntent.data = intent.data
										}
										// This may throw an error. Gotta be super careful
										"0" -> customIntent.clipData = ClipData.newRawUri("Copied",intent.clipData!!.getItemAt(0).uri)
										// This copies the string index value, and converts it to an int to extract and attach said URI
										else -> customIntent.clipData!!.addItem(intent.clipData!!.getItemAt(jsonDataKeys.getString(index).toInt()))
									}
								}
							}
							// Textual AF
							else -> customIntent.putExtra(key,processSettingsJSON.getString(key))
						}
					}
                // If there is a custom setting for this module/intent
				}else{
					// Blank out the intent or nah
					when(customLedger.has("BLANK")){
						// I don't think this should *literally* be blank...
						true -> returnSapphireService(Intent())
						// Send it off, as is.
						false -> returnSapphireService(customIntent)
					}
				}
				Log.d("Sending out a customIntent, action ${customIntent.action}")
				Log.d("This is where it is going to: ${customIntent.getStringExtra("TO")}")
				Log.d("Should have Uri for ${customIntent.getStringArrayListExtra(DATA_KEYS)}")
				returnSapphireService(customIntent)
				intentLedger.put(multiprocessIntent.getIntExtra(MULTIPROCESS_ID,-1).toString(), intentRecord)
			}
		// This is just the unchanged old code
		}else{
			// The record information for MULTIPROCESS_ID
			var intentRecord = JSONObject()
			var multiprocessIntent = prepareIntent(intent!!)

			// Add the initial intent and its data to storage, to await all results
			storedIntents.add(multiprocessIntent)
			// The size is the index of the related intent, so it can be pulled from StoredIntent
			intentRecord.put("ORIGINAL", storedIntents.size)
			// get the list of routes from the prepared intent
			for (route in multiprocessIntent.getStringArrayExtra("MULTIPROCESS_ROUTE_LIST")!!) {
				var packageClass = route.split(";")
				// Make a placeholder record using the route as the key, and a zero which will be filled by an index from a returned intent with matching FROM
				intentRecord.put(route, 0)
				// does this work with returnSapphireService? No, it'll override the starting information
				// I don't think this was set up right for the new 'bounding' form
				multiprocessIntent.setClassName(packageClass[0], packageClass[1])
				returnSapphireService(multiprocessIntent)
			}
			// Store the record for this MULTIPROCESS_ID
			intentLedger.put(multiprocessIntent.getIntExtra(MULTIPROCESS_ID,-1).toString(), intentRecord)
		}
	}

	// This likely *only* works if the data is coming from the core, as that is the only time the permission applies to ALL uris
	// This should be expected at *ALL* times. Core can be a bridge/pipe/socket between other FileProviders, to ease permission issues
	// I might want to keep DATA_KEYS in case the original has its own use for clipData, so I don't overwrite it.
	fun sendUltimateResult(intentRecord: JSONObject){
		// Load the original intent data
		var resultIntent = Intent(storedIntents.get(intentRecord.getInt("ORIGINAL")))
		// Does this overwrite stuff? I should be careful with this
		var clipData = resultIntent.clipData
		try {
			for (key in intentRecord.keys()) {
				if (key == "ORIGINAL") {
					// Skip this, because we want to handle it at the end. This will ensure the primary data passes through
					continue
				} else {
					// This will retrieve all of the intents for a specific record
					var storedIntent = storedIntents.get(intentRecord.getInt(key))
					// Iterate through all the items in the clipData, and pass them along
					for (index in 0..storedIntent.clipData!!.itemCount) {
						// Append the clipData to the resultIntent
						resultIntent.clipData!!.addItem(storedIntent.clipData!!.getItemAt(index))
					}
				}
			}

			// Scrub the action, since that is now taken care of
			resultIntent.action = ""
			// This is hard coded, but should be moved to the SapphireFrameworkService()
			resultIntent.setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
			// Send it to the core, so that it can continue along the pipeline
			startService(resultIntent)
			// If there is nothing left in the ledger, shut down the service.
			if(intentLedger.length() == 0){
				shutdownIfFinished()
			}
		}catch(exception: Exception){
			Log.d(exception.toString())
		}
	}

	// Make sure it's clean
	fun shutdownIfFinished(){
		unbindService(connection)
		stopSelf()
	}

	fun evaluateReturningIntent(intent: Intent?){
		try{
			// Load the intent recod
			var intentRecord = intentLedger.getJSONObject(intent!!.getIntExtra(MULTIPROCESS_ID,-1)!!.toString())
			storedIntents.add(intent)
			// the FROM is the unqiue ID for an intent from this MULTIPROCESS_ID. The size is the index, conveniently
			intentRecord.put(intent.getStringExtra(FROM),storedIntents.size)
			// Save the new information
			intentLedger.put(intent.getIntExtra(MULTIPROCESS_ID,-1).toString(),intentRecord)
			// Check all the values for a zero. If there is one, keep waiting for inputs. Else, all subprocesses received
			for(key in intentRecord.keys()){
				if(intentRecord.getInt(key) == 0){
					// All finished. Wait fore more intents
					return
				}
			}
			//This multiprocess has finished. remove it from the ledger
			intentLedger.remove(intent!!.getStringExtra(MULTIPROCESS_ID))
			// and ship it out
			sendUltimateResult(intentRecord)

		}catch(exception: Exception){
			Log.d(exception.toString())
		}
	}

	// This is just a convenience method to help make things more readable
	fun prepareIntent(intent: Intent): Intent{
		// Give it an ID for tracking, and generate a mutable Intent
		var preparedIntent = generateId(intent)
		preparedIntent = regexRouteString(preparedIntent)
		preparedIntent = makeMultiprocessList(preparedIntent)
		return preparedIntent
	}

	// This is unique to the Multiprocess Module. I need it to look for the unique () syntax
	// This is *not* recursive, and could be easy to mess up
	fun regexRouteString(intent: Intent): Intent{
		var route = intent.getStringExtra(ROUTE)!!
		Log.d(route)
		// Break out the multiprocess syntax
		var start = route.indexOf("(")+1
		var end = route.indexOf(")",start)
		// Return the multiprocess block
		var multiprocessRoute = route.substring(start,end)
		// Return the rest of the information
		var remainingRoute = route.substring(end+1,)

		// I think it's just easier to pass around the intent right now
		intent.putExtra("MULTIPROCESS_ROUTE",multiprocessRoute)
		var returnModule = "${this.packageName};${this.javaClass.canonicalName},"
		// This is to have it return. I could probably move this to preparedIntent()
		intent.putExtra(ROUTE, returnModule+remainingRoute)
		return intent
	}

	// This simply takes the multiprocess route string and turns it in to a list
	fun makeMultiprocessList(intent: Intent): Intent{
		var preparedIntent = Intent(intent)
		var routeList = preparedIntent.getStringExtra("MULTIPROCESS_ROUTE")!!.split(",")
		// This is ugly, and I don't like it
		preparedIntent.putStringArrayListExtra("MULTIPROCESS_ROUTE_LIST", ArrayList(routeList))
		return preparedIntent
	}

	// This should do some
	fun generateId(intent: Intent?): Intent{
		var id = -1
		do{
			id = Random.nextInt().absoluteValue
		}while((id == -1) and (intentLedger.isNull(id.toString()) == false))

		intent!!.putExtra(MULTIPROCESS_ID,id)
		return intent
	}
}