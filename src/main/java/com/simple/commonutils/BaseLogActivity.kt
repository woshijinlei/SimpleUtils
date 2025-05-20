package com.simple.commonutils

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle

open class BaseLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        log("BaseLogActivity:${hashCode()}", "onCreate1:hashCode: ${savedInstanceState?.hashCode()}")
        super.onCreate(savedInstanceState)
        log("BaseLogActivity:${hashCode()}", "onCreate2: $savedInstanceState")
    }

    override fun onRestart() {
        log("BaseLogActivity:${hashCode()}", "onRestart1")
        super.onRestart()
        log("BaseLogActivity:${hashCode()}", "onRestart2")
    }

    override fun onStart() {
        log("BaseLogActivity:${hashCode()}", "onStart1")
        super.onStart()
        log("BaseLogActivity:${hashCode()}:${hashCode()}", "onStart2")
    }


    override fun onResume() {
        log("BaseLogActivity:${hashCode()}", "onResume1")
        super.onResume()
        log("BaseLogActivity:${hashCode()}", "onResume2")
    }

    override fun onPause() {
        log("BaseLogActivity:${hashCode()}", "onPause1")
        super.onPause()
        log("BaseLogActivity:${hashCode()}", "onPause2")
    }

    override fun onStop() {
        log("BaseLogActivity:${hashCode()}", "onStop1")
        super.onStop()
        log("BaseLogActivity:${hashCode()}", "onStop2")
    }

    override fun onBackPressed() {
        log("BaseLogActivity:${hashCode()}", "onBackPressed1")
        super.onBackPressed()
        log("BaseLogActivity:${hashCode()}", "onBackPressed2")
    }

    override fun finish() {
        log("BaseLogActivity:${hashCode()}", "finish1")
        super.finish()
        log("BaseLogActivity:${hashCode()}", "finish2")
    }

    override fun onDestroy() {
        log("BaseLogActivity:${hashCode()}", "onDestroy1")
        super.onDestroy()
        log("BaseLogActivity:${hashCode()}", "onDestroy2")
    }

    override fun onAttachedToWindow() {
        log("BaseLogActivity:${hashCode()}", "onAttachedToWindow1")
        super.onAttachedToWindow()
        log("BaseLogActivity:${hashCode()}", "onAttachedToWindow2")
    }

    override fun onDetachedFromWindow() {
        log("BaseLogActivity:${hashCode()}", "onDetachedFromWindow1")
        super.onDetachedFromWindow()
        log("BaseLogActivity:${hashCode()}", "onDetachedFromWindow2")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        log("BaseLogActivity:${hashCode()}", "onSaveInstanceState1: hashCode: ${outState.hashCode()}")
        super.onSaveInstanceState(outState)
        log("BaseLogActivity:${hashCode()}", "onSaveInstanceState2:$outState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        log(
            "BaseLogActivity:${hashCode()}",
            "onRestoreInstanceState1: hashCode: ${savedInstanceState.hashCode()}"
        )
        super.onRestoreInstanceState(savedInstanceState)
        log("BaseLogActivity:${hashCode()}", "onRestoreInstanceState2:$savedInstanceState")
    }

    override fun onPostResume() {
        log("BaseLogActivity:${hashCode()}", "onPostResume1")
        super.onPostResume()
        log("BaseLogActivity:${hashCode()}", "onPostResume2")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        log("BaseLogActivity:${hashCode()}", "onWindowFocusChanged1:$hasFocus")
        super.onWindowFocusChanged(hasFocus)
        log("BaseLogActivity:${hashCode()}", "onWindowFocusChanged2")
    }

    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        log("BaseLogActivity:${hashCode()}", "isTopResumedActivity1:$isTopResumedActivity")
        super.onTopResumedActivityChanged(isTopResumedActivity)
        log("BaseLogActivity:${hashCode()}", "isTopResumedActivity2")
    }
}
