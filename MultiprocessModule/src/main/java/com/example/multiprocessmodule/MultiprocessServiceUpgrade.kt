package com.example.multiprocessmodule

import android.content.ClipData
import android.content.Intent
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
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
        when{
            intent == null -> Log.d("There was some error, there is no intent!")
            intent.hasExtra(MULTIPROCESS_ID) -> evaluateReturningIntent(intent)
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

        // If there are any settings that should apply to ALL custom intents, set it here (BLANK)
        var templateIntent = setGlobalValues(intent,customProcessLedger)
        //Process each intent
        for(process in processList){
            when(customProcessLedger.has(process)){
                true -> handleCustomProcess(templateIntent,customProcessLedger.getString(process))
                false -> handleStandardProcess(intent)
            }
        }
    }

    fun handleCustomProcess(intent: Intent,ledgerString: String){
        // I did this here cause I don't like super long lines in my code. May change it later
        var settings = JSONObject(ledgerString)
        // Make the customIntent, and tailor it as needed
        var customIntent = Intent(intent)
        customIntent = setLocalSettings(customIntent,settings)
        customIntent = copyUriClipData(customIntent,settings)
        startService(customIntent)
    }

    // These are all broken up for readability
    fun setGlobalValues(intent: Intent, ledger: JSONObject): Intent{
        when{
            // So far, the only global setting is to BLANK all custom intents
            // I will need to transfer the multiprocess ID
            ledger.has("BLANK") -> return Intent()
            else -> return intent
        }
    }

    // This handles extras, actions, and the like
    fun setLocalSettings(intent: Intent, settings: JSONObject): Intent{
        // This is just to give me something mutable if need be
        var customIntent = Intent(intent)
        // Handle special flags, and just copy unk extras
        for(key in settings.keys()) {
            // When the key is a special key, do something, else default
            Log.d("Checking key ${key}")
            when (key) {
                // Set the action for the intent
                "ACTION" -> {
                    customIntent.action = settings.getString(key)
                    Log.d(customIntent.action!!)
                }
                // This inject the route in before returning to multiprocess.
                "ROUTE" -> customIntent.putExtra(ROUTE, settings.getString(ROUTE))
                // Convert the keys to something useable by the next intent
                "MODULE" -> customIntent.putExtra("TO", settings.getString("MODULE"))
            }
        }
        return customIntent
    }

    // Copy the file Uris that apply to this specific intent and route
    fun copyUriClipData(intent: Intent, settings: JSONObject): Intent {
        var customIntent = Intent(intent)
        var dataKeys = mutableListOf<String>()
        var jsonDataKeys = settings.getJSONArray(DATA_KEYS)
        // Set up the intent properly
        customIntent.putExtra(DATA_KEYS,dataKeys.toCollection(ArrayList()))
        // Can't forget to convert length to index
        for (index in 0..jsonDataKeys.length()-1) {
            // This retrieves the file uris to send along in this custom intent
            dataKeys.add(jsonDataKeys.getString(index))
            // I need to copy data w/ a negative -1 because the data is specific to the intent.
            when(jsonDataKeys.getString(index)){
                "-1" -> customIntent.data = intent.data
                "0" -> customIntent.clipData = ClipData.newRawUri("Copied",intent.clipData!!.getItemAt(0).uri)
                else -> customIntent.clipData!!.addItem(intent.clipData!!.getItemAt(jsonDataKeys.getString(index).toInt()))
            }
        }


        return intent
    }

    /*--------------------Returned Multiprocess Intents-------------------*/

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
                    // This needs to account for the normal Data as well!!!!
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

    /*--------------------Utility Functions-----------------*/

    // This is just a convenience method to help make things more readable
    fun handleStandardProcess(intent: Intent): Intent{
        // Give it an ID for tracking, and generate a mutable Intent
        var preparedIntent = generateId(intent)
        preparedIntent = regexRouteString(preparedIntent)
        preparedIntent = makeMultiprocessList(preparedIntent)
        return preparedIntent
    }

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

    // Make sure it's clean
    fun shutdownIfFinished(){
        //unbindService(connection)
        stopSelf()
    }
}