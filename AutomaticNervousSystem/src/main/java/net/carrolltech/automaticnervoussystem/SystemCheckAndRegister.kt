package net.carrolltech.automaticnervoussystem

import android.content.Intent
import net.carrolltech.nervoussystemlibrary.NervousSystemService
import org.json.JSONObject

class SystemCheckAndRegister: NervousSystemService(){

    override fun initModule() {
        super.initModule()
        checkAllModules()
        sendTable()
    }

    fun checkAllModules(){

    }

    fun sendTable(){
        var table = JSONObject().toString()

        var intent = Intent()
        intent.action = "SAVE"
        intent.putExtra("TABLE",table)
    }
}