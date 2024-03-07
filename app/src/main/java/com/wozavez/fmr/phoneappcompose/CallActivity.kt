package com.wozavez.fmr.phoneappcompose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.wozavez.fmr.phoneappcompose.ui.theme.PhoneAppComposeTheme
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.Timer
import java.util.concurrent.TimeUnit

class CallActivity : ComponentActivity() {
    private val disposables = CompositeDisposable()
    private var number = ""
    private val timer = Timer()
    private var isSpeakerOn = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        number = intent.data?.schemeSpecificPart ?: ""
        setContent {
            PhoneAppComposeTheme {
                InCallUI()
            }
        }
    }

    override fun onStart() {
        super.onStart()

//        OngoingCall.state
//            .subscribe(::phoneWithCallInfoDisplayAndButtons)
//            .addTo(disposables)

        OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)
    }

    @Composable
    fun PhoneWithCallInfoDisplayAndButtons(modifier: Modifier = Modifier) {
        //val asd by OngoingCall.state.subscribeAsState()
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
                Text(text = number)

                Button(
                    colors = ButtonDefaults.buttonColors(Color.Red),
                    onClick = {
                        OngoingCall.hangup()
                        finish()
                    }) {
                    Icon(
                        tint = Color.White,
                        painter = painterResource(R.drawable.baseline_call_end_24),
                        contentDescription = "make call"
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun InCallUI() {
        Surface(color = MaterialTheme.colorScheme.background) {
            PhoneWithCallInfoDisplayAndButtons()
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
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