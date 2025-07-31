package com.example.helloworldskill

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SapphireFrameworkRegistrationService

/**
 * Registration service for the HelloWorld skill.
 * 
 * This service handles:
 * 1. Registering the skill with the core framework
 * 2. Providing intent patterns and training data
 * 3. Managing file transfers for configuration
 */
class HelloWorldPostOfficeService : SapphireFrameworkRegistrationService() {
    
    companion object {
        private const val TAG = "HelloWorldPostOffice"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "HelloWorldPostOfficeService started")
        
        intent?.let { processRegistrationIntent(it) }
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Process registration-related intents
     */
    private fun processRegistrationIntent(intent: Intent) {
        val action = intent.getStringExtra("action") ?: ""
        
        when (action) {
            "register" -> registerModule(intent)
            "sendFileNames" -> sendFileNames(intent)
            "coreTransferFile" -> coreTransferFile(intent)
            else -> Log.w(TAG, "Unknown registration action: $action")
        }
    }

    /**
     * Register this module with the core framework
     */
    override fun registerModule(intent: Intent) {
        Log.i(TAG, "Registering HelloWorld skill")
        
        // Register as a SKILL type module
        registerModuleType("SKILL")
        registerVersion("1.0.0")
        
        // Set up routing information
        val responseIntent = Intent().apply {
            putExtra("module", "HelloWorldSkill")
            putExtra("type", "SKILL")
            putExtra("version", "1.0.0")
            putExtra("route", "HelloWorldSkill")
            putExtra("description", "Simple greeting and introduction skill")
        }
        
        returnSapphireService(intent, responseIntent)
        Log.i(TAG, "HelloWorld skill registration complete")
    }

    /**
     * Provide list of files this module offers
     */
    private fun sendFileNames(intent: Intent) {
        Log.d(TAG, "Sending file names")
        
        val files = arrayListOf(
            "hello.intent",
            "greeting.conf"
        )
        
        val responseIntent = Intent().apply {
            putStringArrayListExtra("fileNames", files)
            putExtra("module", "HelloWorldSkill")
        }
        
        returnSapphireService(intent, responseIntent)
    }

    /**
     * Handle file transfer requests from core
     */
    private fun coreTransferFile(intent: Intent) {
        val fileName = intent.getStringExtra("fileName") ?: ""
        Log.d(TAG, "Core requesting file: $fileName")
        
        when (fileName) {
            "hello.intent" -> transferIntentFile(intent)
            "greeting.conf" -> transferConfigFile(intent)
            else -> {
                Log.w(TAG, "Unknown file requested: $fileName")
                returnSapphireService(intent, Intent().apply {
                    putExtra("success", false)
                    putExtra("error", "File not found: $fileName")
                })
            }
        }
    }

    /**
     * Transfer intent patterns file
     */
    private fun transferIntentFile(intent: Intent) {
        val intentPatterns = """
            hello
            hi there
            hey
            good morning
            good afternoon
            good evening
            greetings
            hello (name)
            hi (name)
        """.trimIndent()
        
        writeToCore(intent, intentPatterns)
    }

    /**
     * Transfer configuration file
     */
    private fun transferConfigFile(intent: Intent) {
        val config = """
            {
                "skill_name": "HelloWorldSkill",
                "intents": ["greeting", "introduction"],
                "responses": {
                    "greeting": "Hello! How can I help you?",
                    "introduction": "I'm the Sapphire Assistant Framework!"
                }
            }
        """.trimIndent()
        
        writeToCore(intent, config)
    }
}