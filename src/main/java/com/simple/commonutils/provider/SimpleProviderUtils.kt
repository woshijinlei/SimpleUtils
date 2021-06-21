package com.simple.commonutils.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object SimpleProviderUtils {

    /**
     * @param authority manifest配置的authority
     */
    fun createProviderUri(
        context: Context,
        file: File,
        authority: String = "${context.packageName}.fileprovider"
    ): Uri {
        return FileProvider.getUriForFile(context, authority, file)
    }

}

