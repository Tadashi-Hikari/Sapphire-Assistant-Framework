package com.example.multiprocessmodule

import android.content.ClipData
import android.content.Intent
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue
import kotlin.random.Random

class MultiprocessServiceUpgrade: SapphireFrameworkService(){
    val CUSTOM = "CUSTOM_MULTIPROCESS"
    // This handles all intents, while waiting for multiprocessing to finish
    var storedIntents = mutableListOf<Intent>()
    // This could be replaced w/ core ID
    val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
    // This holds the relavent indexes for each ID
    var intentLedger = JSONObject()
    // This holds specific information about each multiprocess intent

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent == null){
            true -> Log.d(CLASS_NAME,"There was some error, there is no intent!")
            false -> sortNewMultiprocess(intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun sortNewMultiprocess(intent: Intent){
        when{
            intent.hasExtra(CUSTOM) -> handleCustomMultiprocess(intent)
            else -> handleStandardMultiprocess(intent)
        }
    }

    /*--------------------Standard Multiprocess Functions-----------------*/

    fun handleStandardMultiprocess(intent: Intent){
// The record information for MULTIPROCESS_ID
        var intentRecord = JSONObject()
        var multiprocessIntent = handleStandardProcess(intent!!)

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

    /*--------------------Custom Multiprocess Functions-----------------*/

    fun handleCustomMultiprocess(intent: Intent){
        var processList = intent.getStringArrayListExtra("MULTIPROCESS_ROUTE_LIST")!!
        var customProcessLedger = intent.getStringExtra(CUSTOM) as JSONObject

        setGlobalValues()
        //Process each intent
        for(process in processList){
            when(customProcessLedger.has(process)){
                true -> handleCustomProcess(intent)
                false -> handleStandardProcess(intent)
            }
        }
    }

    fun handleCustomProcess(intent: Intent){
        var templateIntent = getTemplateIntent(intent)
        var customIntent = Intent(templateIntent)
        customIntent = setLocalSettings(customIntent)
        customIntent = copyUriData(customIntent)
        startService(customIntent)
    }

    // These are all broken up for readability
    fun setGlobalValues(){

    }

    // This handles extras, actions, and the like
    fun setLocalSettings(intent: Intent): Intent{
        // This doesn't actually do anything. I added this to stop all the red
        var customIntent = Intent(intent)
        // This doesn't actually do anything. I added this to stop all the red
        var processSettingsJSON = JSONObject()
        // Handle special flags, and just copy unk extras
        for(key in processSettingsJSON.keys()) {
            // When the key is a special key, do something, else default
            Log.d(CLASS_NAME, "Checking key ${key}")
            when (key) {
                // Set the action for the intent
                "ACTION" -> {
                    customIntent.action = processSettingsJSON.getString(key)
                    Log.d(CLASS_NAME, customIntent.action!!)
                }
                // This inject the route in before returning to multiprocess.
                "ROUTE" -> customIntent.putExtra(ROUTE, processSettingsJSON.getString(ROUTE))
                // Convert the keys to something useable by the next intent
                "MODULE" -> customIntent.putExtra("TO", processSettingsJSON.getString("MODULE"))
            }
        }

        // This doesn't actually do anything. I added this to stop all the red
        return customIntent
    }

    fun copyUriData(intent: Intent): Intent {
        var customIntent = Intent(intent)
        // This is the format needed for Android
        var dataKeys = mutableListOf<String>()
        // This doesn't actually do anything. I added this to stop all the red
        var processSettingsJSON = JSONObject()
        // These area all of the filenames
        var jsonDataKeys = processSettingsJSON.getJSONArray(DATA_KEYS)
        Log.d(CLASS_NAME, "DATA_KEYS: ${jsonDataKeys}")
        // This is the index of the corrisponding uri in ClipData, or Data
        var jsonDataClipIndex = processSettingsJSON.getJSONObject("DATA_CLIP")
        // Can't forget to convert length to index
        for (index in 0..jsonDataKeys.length() - 1) {
            // This should be the filename
            dataKeys.add(jsonDataKeys.getString(index))
            // When JSONDataKeys has
            when (jsonDataKeys.getString(index)) {
                // Why did I pick negative one?...
                "-1" -> customIntent.data = intent.data
                "0" -> customIntent.clipData = ClipData.newRawUri("Copied", intent.clipData!!.getItemAt(0).uri)
                else -> customIntent.clipData!!.addItem(intent.clipData!!.getItemAt(jsonDataKeys.getString(index).toInt()))
            }
        }

        // This doesn't actually do anything. I added this to stop all the red
        return customIntent
    }

    /*--------------------Utility Functions-----------------*/

    fun getTemplateIntent(intent: Intent): Intent{
        var templateIntent =  Intent()
        return templateIntent
    }

    // This is just a convenience method to help make things more readable
    fun handleStandardProcess(intent: Intent): Intent{
        // Give it an ID for tracking, and generate a mutable Intent
        var preparedIntent = generateId(intent)
        preparedIntent = regexRouteString(preparedIntent)
        preparedIntent = makeMultiprocessList(preparedIntent)
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

    // This is unique to the Multiprocess Module. I need it to look for the unique () syntax
    // This is *not* recursive, and could be easy to mess up
    fun regexRouteString(intent: Intent): Intent{
        var route = intent.getStringExtra(ROUTE)!!
        Log.d(CLASS_NAME,route)
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
}