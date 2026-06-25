package com.digitaldog.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.digitaldog.demo.accessibility.AndroidAnimatorScaleProvider
import com.digitaldog.demo.app.DigitalDogApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val motionPolicy = remember {
                AndroidAnimatorScaleProvider.currentPolicy()
            }

            DigitalDogApp(motionPolicy = motionPolicy)
        }
    }
}
