package com.simple.commonutils

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle

open class BaseLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        log("BaseLogActivity", "onCreate1:hashCode: ${savedInstanceState?.hashCode()}")
        super.onCreate(savedInstanceState)
        log("BaseLogActivity", "onCreate2: $savedInstanceState")
    }

    override fun onRestart() {
        log("BaseLogActivity", "onRestart1")
        super.onRestart()
        log("BaseLogActivity", "onRestart2")
    }

    override fun onStart() {
        log("BaseLogActivity", "onStart1")
        super.onStart()
        log("BaseLogActivity", "onStart2")
    }


    override fun onResume() {
        log("BaseLogActivity", "onResume1")
        super.onResume()
        log("BaseLogActivity", "onResume2")
    }

    override fun onPause() {
        log("BaseLogActivity", "onPause1")
        super.onPause()
        log("BaseLogActivity", "onPause2")
    }

    override fun onStop() {
        log("BaseLogActivity", "onStop1")
        super.onStop()
        log("BaseLogActivity", "onStop2")
    }

    override fun onBackPressed() {
        log("BaseLogActivity", "onBackPressed1")
        super.onBackPressed()
        log("BaseLogActivity", "onBackPressed2")
    }

    override fun finish() {
        log("BaseLogActivity", "finish1")
        super.finish()
        log("BaseLogActivity", "finish2")
    }

    override fun onDestroy() {
        log("BaseLogActivity", "onDestroy1")
        super.onDestroy()
        log("BaseLogActivity", "onDestroy2")
    }

    override fun onAttachedToWindow() {
        log("BaseLogActivity", "onAttachedToWindow1")
        super.onAttachedToWindow()
        log("BaseLogActivity", "onAttachedToWindow2")
    }

    override fun onDetachedFromWindow() {
        log("BaseLogActivity", "onDetachedFromWindow1")
        super.onDetachedFromWindow()
        log("BaseLogActivity", "onDetachedFromWindow2")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        log("BaseLogActivity", "onSaveInstanceState1: hashCode: ${outState.hashCode()}")
        super.onSaveInstanceState(outState)
        log("BaseLogActivity", "onSaveInstanceState2:$outState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        log("BaseLogActivity", "onRestoreInstanceState1: hashCode: ${savedInstanceState.hashCode()}")
        super.onRestoreInstanceState(savedInstanceState)
        log("BaseLogActivity", "onRestoreInstanceState2:$savedInstanceState")
    }
}
