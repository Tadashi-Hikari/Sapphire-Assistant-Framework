package com.example.sapphireassistantframework

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import java.io.File

class CoreContentProvider: ContentProvider(){
    override fun onCreate(): Boolean {
        Log.i(this.javaClass.name, "Created")
        // Should be *very fast running* tasks. Do not block
        return true
    }

    // This adds a *file*
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    // return plainText file type
    override fun getType(uri: Uri): String? {
        return null
    }

    // this deletes a *file*
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    // search for a *file*
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        var file = File(context!!.filesDir,"filename")
        file.writeText("This is a test")
        var temp = arrayOf("filename","size","path")
        var result = MatrixCursor(temp)

        var row = result.newRow()
        row.add("filename",file.name)
        row.add("size",file.length())
        row.add("path",file.absolutePath)
        return result
    }

    // update a *file*. I feel like I don't *really* need this one
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}