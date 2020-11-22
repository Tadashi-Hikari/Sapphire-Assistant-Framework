package com.example.sapphireassistantframework

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import java.util.*

class CoreDatabase(context: Context): BaseColumns, SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    // all of the core tables
    val TABLE_STARTUP = "startup"

    // All of the columns
    val COLUMN_MODULE_NAME = "name"
    val COLUMN_STARTUP_DAEMON = "daemon"
    val COLUMN_PACKAGE_NAME = "package"

    val DEFAULT_PACKAGE_NAME = "www.mabase.tech.mycroft"

    val SQL_CREATE_STARTUP =
            "CREATE TABLE ${this.TABLE_STARTUP} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${this.COLUMN_MODULE_NAME} TEXT," +
                    "${this.COLUMN_PACKAGE_NAME} TEXT," +
                    "${this.COLUMN_STARTUP_DAEMON} BOOLEAN)"

    override fun onCreate(db: SQLiteDatabase){
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        super.onDowngrade(db, oldVersion, newVersion)
    }

    companion object{
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "CoreDatabase.db"
    }

    // Here are all the default values. They're currently hardcoded, but I intend to offload them
    // to a file for Unix sake
    fun initDatabase(){
        var db = this
        var dbHelper = db.writableDatabase

        Log.i("CoreDatabase", "CoreDatabase onCreate() start")
        var tables: Array<String> = arrayOf(TABLE_STARTUP)

        for(table: String in tables){
            dbHelper.execSQL("DROP TABLE IF EXISTS ${table}")
        }

        dbHelper.execSQL(SQL_CREATE_STARTUP)

        // These are all just separated for readability sake
        fillStartup(db, dbHelper)
    }

    fun fillStartup(db: CoreDatabase, dbHelper: SQLiteDatabase){
        Log.i("CoreDatabase", "fillStartup() starting")
        var value: ContentValues = ContentValues()
        var values: ArrayList<ContentValues> = ArrayList()


        // Something is wrong with this. I think it's feeding in wrong
        values.add(ContentValues().apply{
            put(db.COLUMN_PACKAGE_NAME, DEFAULT_PACKAGE_NAME)
            put(db.COLUMN_MODULE_NAME, "com.example.pocketsphinxmodule.KaldiService")
            put(db.COLUMN_STARTUP_DAEMON, 1)
        })

        values.add(ContentValues().apply{
            put(db.COLUMN_PACKAGE_NAME, DEFAULT_PACKAGE_NAME)
            put(db.COLUMN_MODULE_NAME, "UDPClient")
            put(db.COLUMN_STARTUP_DAEMON, 1)
        })


        for(value: ContentValues in values) {
            Log.i("CoreDatabase", value.toString())
            dbHelper?.insert(db.TABLE_STARTUP, null, value)
        }
    }
}