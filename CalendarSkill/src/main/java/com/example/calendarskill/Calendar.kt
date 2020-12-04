package com.example.calendarskill

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class Calendar: Service(){
    // command keywords
    // This covers 1st date and time
    var action = ""
    var dateTimeStart = ""
    // This covers 2nd date and time
    var dateTimeEnd = ""
    var eventName = ""
    var description = ""

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        getManditoryVariables(intent)
        checkForOptional(intent)

        var action = intent.action
        if (action == "create") {
            var message = "Created ${eventName} on ${dateTimeStart}/s"
            if(dateTimeEnd != "") message+="until ${dateTimeEnd}/s"
            if (description != "") message+="with description ${description}"
            Log.i("Calendar",message)

        }else if(action == "retrieve"){
            Log.i("Calendar","Retrieve action")
        }else if(action == "update"){
            Log.i("Calendar","Update action")
        }else{
            Log.i("Calendar","Delete action")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun getManditoryVariables(intent: Intent){
        action = intent.getStringExtra("assistant.skill.calendar.ACTION")!!
        dateTimeStart = intent.getStringExtra("dateTime")!!
        eventName = intent.getStringExtra("event")!!
    }

    fun checkForOptional(intent: Intent){
        if(intent.hasExtra("event")) eventName = intent.getStringExtra("event")!!
        if(intent.hasExtra("description")) description = intent.getStringExtra("description")!!
    }

    fun populateCalendar(intent: Intent){
        if(intent.hasExtra("dateTime1") and (intent.hasExtra("dateTime2"))){
            dateTimeStart = intent.getStringExtra("dateTime1")!!
            dateTimeEnd = intent.getStringExtra("dateTime2")!!
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}