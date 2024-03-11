package com.wozavez.fmr.phoneappcompose

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.wozavez.fmr.phoneappcompose.ui.theme.PhoneAppComposeTheme
import timber.log.Timber
import java.util.Timer
import kotlin.concurrent.timerTask

class CallActivity : ComponentActivity() {
    private var number = ""
    private var isSpeakerOn = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val viewModel: OngoingCallViewModel by viewModels()
        number = intent.data?.schemeSpecificPart ?: ""
        setContent {
            PhoneAppComposeTheme {
                InCallUI(viewModel)
            }
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    @Composable
    fun PhoneWithCallInfoDisplayAndButtons(viewModel: OngoingCallViewModel, modifier: Modifier = Modifier) {
        var selected by remember { mutableStateOf(false) }
        val color = if (selected) Color.Red else Color.Gray

        Box (
            modifier = modifier
                .fillMaxSize()
                .scale(1.8f),
            contentAlignment = Alignment.Center
        ) {
            Column (
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.state == Call.STATE_ACTIVE || viewModel.state == Call.STATE_DISCONNECTING || viewModel.state == Call.STATE_DISCONNECTED) {

                    Text(text = buildString {
                        if (viewModel.time >= 3600) append((viewModel.time/3600).toString() + ":") // hours
                        append((viewModel.time%3600)/600) // 10 minutes
                        append((viewModel.time%600)/60) // minutes
                        append(":")
                        append((viewModel.time%60)/10) // 10 seconds
                        append(viewModel.time%10) // seconds
                    })
                }
                Text(text = viewModel.state.asString())
                Text(text = number)
                
                Row {
                    Button(
                        colors = ButtonDefaults.buttonColors(color),
                        onClick = {
                            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                            audioManager.setMode(AudioManager.MODE_IN_CALL)

                            if (isSpeakerOn) {
                                audioManager.availableCommunicationDevices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                                    ?.let {
                                        Timber.d("changed to earpiece")
                                        val result = audioManager.setCommunicationDevice(it)
                                        Timber.d(result.toString() + " " + audioManager.communicationDevice.toString())
                                    }
                                selected = false
                                isSpeakerOn = false
                            } else {
                                audioManager.availableCommunicationDevices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                                    ?.let {
                                        Timber.d("changed to speaker")
                                        val result = audioManager.setCommunicationDevice(it)
                                        Timber.d(result.toString() + " " + audioManager.communicationDevice.toString())
                                    }
                                selected = true
                                isSpeakerOn = true
                            }

                        }) {
                        Icon(
                            tint = Color.White,
                            painter = painterResource(R.drawable.baseline_speaker_24),
                            contentDescription = "turn on speaker"
                        )
                    }
                    
                    Button(
                        colors = ButtonDefaults.buttonColors(Color.Red),
                        onClick = {
                            viewModel.hangup()

                            val timer = Timer()
                            timer.schedule(timerTask {
                                finish()
                            }, 2000)
                        }) {
                        Icon(
                            tint = Color.White,
                            painter = painterResource(R.drawable.baseline_call_end_24),
                            contentDescription = "end call"
                        )
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun InCallUI(@PreviewParameter(SampleOngoingCallViewModel::class) viewModel:OngoingCallViewModel) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PhoneWithCallInfoDisplayAndButtons(viewModel)
        }
    }

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }
}