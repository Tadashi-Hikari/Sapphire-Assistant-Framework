package com.example.helloworldskill

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SapphireFrameworkService

/**
 * Simple Hello World skill demonstrating the basic Sapphire Framework skill pattern.
 * 
 * This skill responds to greetings and demonstrates:
 * 1. How to extend SapphireFrameworkService
 * 2. How to process incoming intents
 * 3. How to extract parameters from user input
 * 4. How to respond back to the user
 */
class HelloWorldService : SapphireFrameworkService() {
    
    companion object {
        private const val TAG = "HelloWorldService"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "HelloWorldService started")
        
        intent?.let { processIntent(it) }
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Process the incoming intent and determine what action to take
     */
    private fun processIntent(intent: Intent) {
        val action = intent.getStringExtra("action") ?: ""
        val utterance = intent.getStringExtra("utterance") ?: ""
        
        Log.d(TAG, "Processing action: $action, utterance: $utterance")
        
        when (action) {
            "greeting" -> handleGreeting(intent)
            "introduction" -> handleIntroduction(intent)
            else -> handleUnknown(intent)
        }
    }

    /**
     * Handle greeting intents like "hello", "hi", "hey"
     */
    private fun handleGreeting(intent: Intent) {
        val name = intent.getStringExtra("name") ?: "there"
        val response = "Hello $name! I'm the Sapphire Assistant. How can I help you today?"
        
        Log.i(TAG, "Greeting response: $response")
        
        // Send response back to core
        returnResponse(intent, response)
    }

    /**
     * Handle introduction requests like "who are you", "what are you"
     */
    private fun handleIntroduction(intent: Intent) {
        val response = "I'm the Sapphire Assistant Framework, an open-source voice assistant " +
                "that works completely offline. I'm here to help you with various tasks!"
        
        Log.i(TAG, "Introduction response: $response")
        
        // Send response back to core
        returnResponse(intent, response)
    }

    /**
     * Handle unknown or unsupported requests
     */
    private fun handleUnknown(intent: Intent) {
        val response = "I'm sorry, I don't understand that command yet. " +
                "Try saying 'hello' or asking 'who are you'."
        
        Log.i(TAG, "Unknown command response: $response")
        
        // Send response back to core
        returnResponse(intent, response)
    }

    /**
     * Send response back to the core system
     */
    private fun returnResponse(originalIntent: Intent, response: String) {
        val responseIntent = Intent().apply {
            putExtra("response", response)
            putExtra("success", true)
            putExtra("module", "HelloWorldSkill")
        }
        
        // Use the framework's response mechanism
        returnSapphireService(originalIntent, responseIntent)
    }
}