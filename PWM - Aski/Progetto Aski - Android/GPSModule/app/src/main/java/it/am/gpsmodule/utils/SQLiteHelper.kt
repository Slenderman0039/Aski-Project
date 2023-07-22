package it.am.gpsmodule.utils

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.content.contentValuesOf
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
const val DATABASE_NAME = "db_local_project.db" /* the database name */
const val ASSET_NAME = "db_local_project.db" /* The name of the asset file which could be different if required */
const val DATABASE_VERSION = 1
const val ASSET_COPY_BUFFER_SIZE = 8 * 1024
class SQLiteHelper: SQLiteOpenHelper {

    private constructor(context: Context) : super(context, DATABASE_NAME, null, DATABASE_VERSION)

    companion object {
        private var instance: SQLiteHelper? = null
        fun getInstance(context: Context): SQLiteHelper {
            if (this.instance == null) {
                getAndCopyAssetDatabase(context)
                instance = SQLiteHelper(context);
            }
            return instance as SQLiteHelper
        }

        private fun ifDatabaseExists(context: Context): Boolean {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (dbFile.exists()) return true
            else if (!dbFile.parentFile.exists()) {
                dbFile.parentFile.mkdirs()
            }
            return false
        }

        private fun getAndCopyAssetDatabase(context: Context) {
            if (ifDatabaseExists(context)) return
            context.assets.open(ASSET_NAME).copyTo(
                FileOutputStream(context.getDatabasePath(DATABASE_NAME)),
                ASSET_COPY_BUFFER_SIZE
            )
        }
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        // should not do anything if using a pre-packaged database
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // May or may not be used
    }
}
