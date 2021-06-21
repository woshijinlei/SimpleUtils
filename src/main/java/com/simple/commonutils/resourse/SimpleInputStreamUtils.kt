package com.simple.commonutils.resourse

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.lang.Exception

@Suppress("MemberVisibilityCanBePrivate")
object SimpleInputStreamUtils {

    fun loadAsset(context: Context, path: String): InputStream {
        return context.assets.open(path)
    }

    fun loadRaw(context: Context, rawResourceId: Int): InputStream {
        return context.resources.openRawResource(rawResourceId)
    }

    fun loadRes(context: Context, resId: Int): InputStream? {
        return try {
            context.contentResolver.openInputStream(
                Uri.parse("android.resource://${context.packageName}/$resId")
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 支持的协议：
     * content:
     * file:
     * android_asset:
     * resource:
     */
    fun loadUri(context: Context, uri: Uri): InputStream? {
        return try {
            val path = uri.toString()
            when {
                path.contains("android_asset") -> {
                    parseAssetUri(
                        uri
                    )?.let { loadAsset(context, it) }
                }
                path.contains("rawresource") -> {
                    loadRaw(context, parseRawUri(uri))
                }
                else -> {
                    context.contentResolver.openInputStream(uri)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseAssetUri(path: Uri): String? {
        val p = path.toString()
        val index = p.indexOf("android_asset")
        if (index == -1) {
            return null
        }
        return p.substring(index + "android_asset".length + 1)
    }

    private fun parseRawUri(path: Uri): Int {
        val p = path.toString()
        return p.substringAfter("///").toInt()
    }
}