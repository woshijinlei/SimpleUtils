package com.simple.commonutils.provider

import androidx.core.content.FileProvider

/**
 * <provider
 *  android:name="com.example.myapplication.xxxProvider"
 *  android:authorities="${applicationId}.fileprovider"
 *  android:exported="false"
 *  android:grantUriPermissions="true">
 *      <meta-data
 *          android:name="android.support.FILE_PROVIDER_PATHS"
 *          android:resource="@xml/xxx_provider_paths" />
 * </provider>
 */
class CommonProvider : FileProvider()