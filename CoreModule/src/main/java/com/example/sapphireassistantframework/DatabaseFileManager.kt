package com.example.sapphireassistantframework

import android.app.Service
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DatabaseFileManager: Service(){
    var databaseFilename = "CoreDatabaseFile.db"
    var databaseFile = File(filesDir,databaseFilename)

    var routeJSON = JSONObject()
    var aliasJSON = JSONArray()
    var databaseJSON = JSONObject()

    fun addRoute(){
    }

    fun removeRoute(){

    }

    fun copyRoute(){

    }
}