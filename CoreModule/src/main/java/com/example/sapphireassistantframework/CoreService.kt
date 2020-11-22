package com.example.sapphireassistantframework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.stanford.nlp.classify.ColumnDataClassifier
import java.lang.Exception
import java.util.*
import org.json.JSONObject
import java.io.*

internal class CoreService : Service(){
    private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "SAF"
    private val NAME = "Sapphire Assistant Framework"
    private val SERVICE_TEXT = "Sapphire Assistant Framework"
    private var sapphire_apps: LinkedList<Pair<String, String>> = LinkedList()
    private lateinit var classifier: ColumnDataClassifier

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun scanApplicationManifests(){
        var package_manager: PackageManager = packageManager
        var installed_packages = package_manager.getInstalledPackages(0)

        for(package_info: PackageInfo in installed_packages){
            var application_info = package_manager.getApplicationInfo(package_info.packageName,PackageManager.GET_META_DATA)
            var bundle = application_info.metaData
            try {
                if (bundle.containsKey("sapphire_assistant_framework_module")) {
                    Log.i("CoreService", "Found a SAF module")
                    Log.i("CoreService", application_info.packageName)
                    var package_name = application_info.packageName
                    if (bundle.containsKey("bound_service")) {
                        Log.i("CoreService", bundle.getString("bound_service", "Error"))
                        var classname = bundle.getString("bound_service")
                        // Checking for null explicitly lets me avoid those pesky Kotlin issues
                        if(package_name != null && classname != null){
                            var app_data = Pair(package_name,classname)
                            sapphire_apps.add(app_data)
                        }
                    }
                }
            }catch(e: Exception){
                continue
            }
        }

        Log.i("CoreService","Returning found SAF modules")
    }

    fun update_database(sapphire_apps: List<Pair<String,String>>): List<Pair<String,String>>{
        Log.i("CoreService; update_database()","Not yet implemented. Dummy code")

        return sapphire_apps
    }

    override fun onCreate() {
        super.onCreate()
        classifier = train()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == "sapphire_assistant_framework.BIND") {
            createNotificationChannel()
            with(NotificationManagerCompat.from(this)) {
                // I defined notification id as 1, may need to change this later
                notify(1337, builder.build())
            }

            //this isn't working unless its in the Daemon. Maybe its the context is service specific,
            //rather than app specific
            var db = CoreDatabase(this.applicationContext)
            db.initDatabase()

            scanApplicationManifests()
            update_database(sapphire_apps)
            //startup_services = load_startup_services()


            // Start all of the other Mycroft/SAF services
            // I want to see this starting the services from the database
            // Namely, I expect to see the UDP_Server started
            startBackgroundServices(sapphire_apps)
            // This may be a much longer running service than I was expecting
            Log.i("CoreService", "Everything should be starting up now")
        } else {
            intentHandler(intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // This is the notification for the foreground service. Maybe have it lead into other bound services
    var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.assistant)
            .setContentTitle("Sapphire Assistant Framework")
            .setContentText("SAF is running")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

    // I think I need to change up some of this
    fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, NAME, importance).apply {
                description = SERVICE_TEXT
            }

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundServices(sapphire_apps, connections)
        notificationManager.cancel(1337)
    }

    //Called 'post_office' in Python version
    // Strikes me as inefficent. Also, the path probably shouldn't be hardcoded
    // I suppose this is the place to read the config file, or search for installed modules
    // Maybe I should move this to its own file
    fun intentHandler(intent: Intent) {
        Log.i("CoreService", "IntentHandler received an intent")
        var options: LinkedList<String> = LinkedList<String>()
        options.add("HYPOTHESIS")
        for (extra: String in options.iterator()) {
            Log.i("CoreService", "Testing for extra ${extra}")
            if (intent.hasExtra(extra) == true) {
                var value = intent.getStringExtra(extra)
                Log.i("CoreService", "${value}")
                // Load task from some database
                // Execute task (intent?)
                if (extra.equals("HYPOTHESIS")){
                    if (value != null) updateUtterance(value)
                }
            }
        }
    }

    // This is just updating the UI. I need to make this more dynamic I think
    fun updateUtterance(utterance: String){
        var coreCentralActivityIntent = Intent()
        //coreCentralActivityIntent.setClassName(this, "${packageName}.CoreCentralActivity")
        coreCentralActivityIntent.setAction("UPDATE")

        var json = JSONObject(utterance)
        var text: String = json.getString("text")
        coreCentralActivityIntent.putExtra("HYPOTHESIS",text)
        sendBroadcast(coreCentralActivityIntent)
        Log.i("CoreService", "Text is ${text}")

        if(text != "") {
            // Something isn't right here, and I'm not quite sure what. Does it matter the testing class? Maybe not.
            var temp = "none\t${text}"
            var datum = classifier.makeDatumFromLine(temp)
            var classified = classifier.classOf(datum)
            Log.i("CoreService", classified)
            var counter = classifier.scoresOf(datum)
            var score = counter.getCount(classified)
            Log.i("CoreService", "${counter.keySet()}")
            Log.i("CoreService", "${counter.values()}")
            if(classifier.scoresOf(datum).getCount(classified) > 1.0){
                Log.i("CoreService","${classified}, ${score}")
            }else{
                Log.i("CoreService", "Does not match a class")
            }
        }
    }

    // Can I collapse all of this database stuff into CoreDatabase, rather than here?
    fun startBackgroundServices(sapphire_apps: List<Pair<String,String>>){
        Log.i("CoreService", "Starting startupServices()")
        var dbHelper = CoreDatabase(this.applicationContext)
        var db = dbHelper.readableDatabase

        // get all of the services from the table
        val cursor = db.query(
                dbHelper.TABLE_STARTUP,
                null,
                null,
                null,
                null,
                null,
                null
        )
        //
        Log.i("CoreDaemon", "Number of records: ${cursor.count}")

        var daemon = cursor.getColumnIndex(dbHelper.COLUMN_STARTUP_DAEMON)
        // may need to change this to COLUMN_CLASS_NAME
        var pkg = cursor.getColumnIndex(dbHelper.COLUMN_PACKAGE_NAME)
        var classname = cursor.getColumnIndex(dbHelper.COLUMN_MODULE_NAME)


        for(info_pair: Pair<String,String> in sapphire_apps){
            // Do I need to return a pair? Seems annoying.
            Log.i("CoreService","launching ${info_pair.first}, ${info_pair.second}")
            connections.plus(Pair(info_pair.second,startBoundService(info_pair.first,info_pair.second)))
        }
    }

    fun stopBackgroundServices(sapphire_apps: List<Pair<String,String>>, connections: LinkedList<Pair<String,CoreService.Connection>>){
        Log.i("CoreService", "Starting startupServices()")
        var dbHelper = CoreDatabase(this.applicationContext)
        var db = dbHelper.readableDatabase

        // get all of the services from the table
        val cursor = db.query(
                dbHelper.TABLE_STARTUP,
                null,
                null,
                null,
                null,
                null,
                null
        )
        //
        Log.i("CoreDaemon", "Number of records: ${cursor.count}")

        var daemon = cursor.getColumnIndex(dbHelper.COLUMN_STARTUP_DAEMON)
        // may need to change this to COLUMN_CLASS_NAME
        var pkg = cursor.getColumnIndex(dbHelper.COLUMN_PACKAGE_NAME)
        var classname = cursor.getColumnIndex(dbHelper.COLUMN_MODULE_NAME)


        for(connection in connections){
            var service = connection.second
            unbindService(service)
        }
        for(info_pair: Pair<String,String> in sapphire_apps){
            // Do I need to return a pair? Seems annoying.
            Log.i("CoreService","launching ${info_pair.first}, ${info_pair.second}")
            var intent = Intent().setClassName(info_pair.first, info_pair.second)
            stopService(intent)
        }
    }

    // start these services as threads? Can I start them in their own process?
    // I am just going to broadcast that Mycroft has started. It can ping core for bindings if need be
    fun startBoundService(pkg: String, classname: String): Connection{
        var connection =  bindToService(pkg, classname)

        return connection
    }

    fun bindToService(pkg: String, classname: String): Connection{
        Log.i("CoreService", "binding ${pkg}, ${classname}")
        // This needs to be changed to not pseudocode
        var connection = Connection()
        // This will likely need to change over time
        // Package name is right. Is the class name?
        var coreService: Intent = Intent().setAction("mycroft.BIND")
        // There is an issue with the module name and class name. I will need to fix this
        coreService.setClassName(pkg, classname)
        coreService.putExtra("CORE_PACKAGE", "www.mabase.tech.mycroft")
        coreService.putExtra("CORE_CLASSNAME", "www.mabase.tech.mycroft.CoreDaemon")
        if(coreService.resolveActivity(packageManager) != null) {
            bindService(coreService, connection, Context.BIND_AUTO_CREATE)
        }else{
            Log.e("CoreDaemon", "PackageManager says the service doesn't exist")
        }

        return connection
    }

    // The bound connection. The core attaches to each service as a client, tying them to cores lifecycle
    inner class Connection(): ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i("CoreService", "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("CoreService", "Service disconnected")
        }
    }

    // Uses a tab-delimited text
    fun train(): ColumnDataClassifier{
        // I should probably make this a resource name
        var propFile = convertStreamToFile("test",".prop")
        var trainingFile = convertStreamToFile("training_data",".txt")
        Log.i("CoreService", "The conversion is done")

        //var classifier = ColumnDataClassifier(propFile.canonicalPath)
        var props = createProperties()
        var classifier = ColumnDataClassifier(props)
        classifier.trainClassifier(trainingFile.canonicalPath)
        Log.i("CoreService", "The training is done")

        return classifier
    }

    fun createProperties(): Properties{
        var props = Properties()
        props.setProperty("goldAnswerColumn","0")
        props.setProperty("useNB","true")
        //props.setProperty("useClass","true")
        props.setProperty("useClassFeatures","true")
        //props.setProperty("1.splitWordsRegexp","false")
        //props.setProperty("1.splitWordsTokenizerRegexp","false")
        props.setProperty("1.splitWordsWithPTBTokenizer","true")
        // This is the line that was missing
        props.setProperty("1.useSplitWords","true")

        return props
    }

    // I hate that Android won't let me bundle files as files
    fun convertStreamToFile(prefix: String, suffix: String): File{
        // This file needs to be tab separated columns
        var asset = assets.open(prefix+suffix)
        var fileReader = InputStreamReader(asset)

        var tempFile = File.createTempFile(prefix, suffix)
        var tempFileWriter = FileOutputStream(tempFile)
        // This is ugly AF
        var data = fileReader.read()
        while (data != -1) {
            tempFileWriter.write(data)
            data = fileReader.read()
        }
        // Do a little clean up
        asset.close()
        tempFileWriter.close()

        return tempFile
    }
}