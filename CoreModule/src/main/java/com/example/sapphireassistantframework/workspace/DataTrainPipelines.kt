package com.example.sapphireassistantframework.workspace

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SAFService

class DataTrainPipelines: SAFService() {
    val SEQUENCE_NUMBER = "core.conf.framework.protocol.SEQUENCE_NUMBER"
    val ID = "core.conf.framework.protocol.ID"

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun DataTrainingPipeline(){
        // List all programs that have processor data
        // Look up the pipelines for the processors
        var module = "com.example.somedatamodule.InstallService"
        var dataModules = mutableListOf<String>()
        if(module == "core.conf.framework.processor.DATA"){
            dataModules.add(module)
        }

        var processorModules = listProcessorModules()
        var numberOfModules = processorModules.count()
        var counter = 0
        var id = assignID()
        for(module in processorModules){
            counter++
            var trainingIntent = Intent()
            var pipelineData = getTrainingPipeline(module)
            var pipeline = parseRoute(pipelineData)
            trainingIntent.putExtra(ROUTE,pipelineData)
            trainingIntent.putExtra(SEQUENCE_NUMBER, "${counter}:${numberOfModules}")
            trainingIntent.putExtra(ID,id)
            trainingIntent.setClassName(this,getNextAlongRoute(pipeline))
            /**
             * This sends off an intent that is meant to be aggrigated, upon aggregation it gets
             * aggregated and sent along to the processor to be filtered as needed
              */
            startService(trainingIntent)
        }
    }

    fun assignID(): Int{
        return 1
    }

    fun getTrainingPipeline(module: String): String{
        return ""
    }

    fun listProcessorModules(): List<String>{
        return emptyList()
    }
}