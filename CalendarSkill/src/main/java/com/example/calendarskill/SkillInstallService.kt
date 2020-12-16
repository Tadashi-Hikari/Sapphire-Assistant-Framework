package com.example.calendarskill

import android.content.Intent
import android.util.Log
import com.example.calendarskill.HelperFiles.InstallHelper

class SkillInstallService: InstallHelper(){
    var parserFiles = arrayOf("date.entity","get.intent","NER.entity","time.entity")

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action == "assistant.framework.module.INSTALL") {
            Log.i("SkillInstallService","Install intent received. Installing CalendarSkill")
            registerSkill()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun registerSkill(){
        /**
        var trainParser = Intent()
        trainParser.setAction(ACTION_TRAIN_PARSER)
        trainParser.setClassName(PARSER_MODULE_PACKAGE,PARSER_MODULE_INSTALL_CLASS)
        trainParser.putExtra("VERSION",PARSER_VERSION)
        trainParser.fillIn(attachTrainingFiles(parserFiles),0)
        attachTrainingFiles(parserFiles)

        /** This is in the parser itself. Should this be so hard coded? Should there be some
         * indicator that tell the core service what it needs to send?
         */

        startService(trainParser)
        **/
    }
}