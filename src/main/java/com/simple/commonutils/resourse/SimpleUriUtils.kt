package com.simple.commonutils.resourse

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.MediaStore

object SimpleUriUtils {
    fun createAssetUri(path: String): Uri {
        return Uri.parse("file:///android_asset/$path")
    }

    fun createResUri(context: Context, resId: Int): Uri {
        return Uri.parse("android.resource://${context.packageName}/$resId")
    }

    fun createRawUri(rawResourceId: Int): Uri {
        return Uri.parse("rawresource:///$rawResourceId")
    }

    /**
     * This method expects uri in the following format
     *     content://media/<table_name>/<row_index> (or)
     *     file://sdcard/test.mp4
     *     http://test.com/test.mp4
     *     https://test.com/test.mp4
     *
     * Here <table_name> shall be "video" or "audio" or "images"
     * <row_index> the index of the content in given table
     */
    fun convertUriToPath(context: Context, uri: Uri?): String? {
        var path: String? = null
        if (null != uri) {
            val scheme = uri.scheme
            if (null == scheme || scheme == "" || scheme == ContentResolver.SCHEME_FILE) {
                path = uri.path
            } else if (scheme == "http" || scheme == "https") {
                path = uri.toString()
            } else if (scheme == ContentResolver.SCHEME_CONTENT) {
                val projection =
                    arrayOf(MediaStore.MediaColumns.DATA)
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(
                        uri, projection, null,
                        null, null
                    )
                    require(!(null == cursor || 0 == cursor.count || !cursor.moveToFirst())) {
                        "Given Uri could not be found" +
                                " in media store"
                    }
                    val pathIndex =
                        cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    path = pathIndex?.let { cursor?.getString(it) }
                } catch (e: SQLiteException) {
                    throw IllegalArgumentException(
                        "Given Uri is not formatted in a way " +
                                "so that it can be found in media store."
                    )
                } finally {
                    cursor?.close()
                }
            } else {
                throw IllegalArgumentException("Given Uri scheme is not supported")
            }
        }
        return path
    }
}