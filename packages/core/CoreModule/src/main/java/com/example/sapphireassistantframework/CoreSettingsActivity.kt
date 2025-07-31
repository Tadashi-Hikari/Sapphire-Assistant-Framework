package com.example.sapphireassistantframework

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.lang.Exception

class CoreSettingsActivity : AppCompatActivity() {

	val CONFIG = "sample-core-config.conf"
	lateinit var uri: Uri

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.settings_activity)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
			var textView: TextView = findViewById(R.id.directory)
			uri = data!!.data!!
			textView.setText(uri.path)
		}
	}

	fun toggleExport(view: View){
		var jsonConfig = JSONObject()
		var file = File(filesDir, CONFIG)
		if(file.exists()) {
			jsonConfig = JSONObject(file.readText())
			try{
				var exported = jsonConfig.getBoolean("exported")
				if(exported){
					var exportSwitch: Switch = findViewById(R.id.export)
					exportSwitch.toggle()
				}
				if(jsonConfig.has("uri")) {
					var textView: TextView = findViewById(R.id.directory)
					textView.setText(jsonConfig.getString("uri"))
				}
			}catch(exception:Exception){
				Log.e(this.javaClass.name,"There was an error getting the boolean exported value")
			}
		}else{
			jsonConfig.put("exported",true)
			var pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
			startActivityForResult(pickerIntent,1)
		}

		file.writeText(jsonConfig.toString())
	}
}