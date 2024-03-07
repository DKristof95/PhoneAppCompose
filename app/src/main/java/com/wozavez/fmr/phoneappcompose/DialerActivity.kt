package com.wozavez.fmr.phoneappcompose

import android.Manifest.permission.CALL_PHONE
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import com.wozavez.fmr.phoneappcompose.ui.theme.PhoneAppComposeTheme

class DialerActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        offerReplacingDefaultDialer()
        setContent {
            PhoneAppComposeTheme {
                DialerUI()
            }
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    @Composable
    fun PhoneWithNumberDisplayAndDial(modifier: Modifier = Modifier) {
        var displayValue by remember {
            mutableStateOf("")
        }
        val viewConfiguration = LocalViewConfiguration.current
        val interactionSourceBalance = remember { MutableInteractionSource() }
        val interactionSourceNumber = remember { MutableInteractionSource() }
        val interactionSourceBackspace = remember { MutableInteractionSource() }

        LaunchedEffect(interactionSourceBalance) {
            var isLongClick = false

            interactionSourceBalance.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        isLongClick = false
                        delay(viewConfiguration.longPressTimeoutMillis)
                        isLongClick = true
                        makeCall("*102#")
                    }

                    is PressInteraction.Release -> {
                        if (isLongClick.not()) {
                            displayValue += "1"
                        }
                    }
                }
            }
        }

        LaunchedEffect(interactionSourceNumber) {
            var isLongClick = false

            interactionSourceNumber.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        isLongClick = false
                        delay(viewConfiguration.longPressTimeoutMillis)
                        isLongClick = true
                        makeCall("*121#")
                    }

                    is PressInteraction.Release -> {
                        if (isLongClick.not()) {
                            displayValue += "2"
                        }
                    }
                }
            }
        }

        LaunchedEffect(interactionSourceBackspace) {
            var isLongClick = false

            interactionSourceBackspace.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        isLongClick = false
                        delay(viewConfiguration.longPressTimeoutMillis)
                        isLongClick = true
                        displayValue = ""
                    }

                    is PressInteraction.Release -> {
                        if (isLongClick.not()) {
                            displayValue = displayValue.dropLast(1)
                        }
                    }
                }
            }
        }

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
                // Display Area
                Text(
                    modifier = modifier,
                    text = displayValue,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = modifier.height(16.dp)
                )

                // Dial Area
                // First Row with special Buttons
                Row(
                    modifier = modifier
                ) {
                    TextButton(
                        onClick = {},
                        interactionSource = interactionSourceBalance
                    ) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text("1", color = MaterialTheme.colorScheme.inverseSurface)
                            Text(stringResource(R.string.balance_buttonHUN), fontSize = 5.sp, color = MaterialTheme.colorScheme.inverseSurface)
                        }
                    }
                    TextButton(
                        onClick = {},
                        interactionSource = interactionSourceNumber
                    ) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text("2", color = MaterialTheme.colorScheme.inverseSurface)
                            Text(stringResource(R.string.number_buttonHUN), fontSize = 5.sp, color = MaterialTheme.colorScheme.inverseSurface)
                        }
                    }
                    TextButton(onClick = { displayValue += "3" }) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text("3", color = MaterialTheme.colorScheme.inverseSurface)
                            Text("", fontSize = 5.sp, color = MaterialTheme.colorScheme.inverseSurface)
                        }
                    }
                }
                // Second and third Row with regular Buttons
                for (i in 1..2) {
                    Row(
                        modifier = modifier
                    ) {
                        for (j in 1..3) {
                            TextButton(onClick = { displayValue += i*3+j }) {
                                Text(text = (i*3+j).toString(), color = MaterialTheme.colorScheme.inverseSurface)
                            }
                        }
                    }
                }
                // Third Row with 0
                Row(
                    modifier = modifier
                ) {
                    TextButton(modifier = modifier,
                        onClick = { displayValue += "0" }) {
                        Text(text = "0", color = MaterialTheme.colorScheme.inverseSurface)
                    }
                }
                // Last Row with Call and Backspace Buttons and a placeholder Button
                Row(
                    modifier = modifier
                ) {
                    Button(modifier = modifier.alpha(0f), onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_phone_24),
                            contentDescription = "placeholder")
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(Color.Green),
                        onClick = {
                            if (displayValue.length >= 10 && (displayValue.startsWith("06") || displayValue.startsWith("00"))) {
                                makeCall(displayValue)
                            }
                        }) {
                        Icon(
                            tint = Color.White,
                            painter = painterResource(R.drawable.baseline_phone_24),
                            contentDescription = "make call")
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                        modifier = modifier.alpha(if (displayValue.isNotEmpty()) 1f else 0f),
                        interactionSource = interactionSourceBackspace,
                        onClick = {}
                    ) {
                        Icon(
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            painter = painterResource(R.drawable.baseline_backspace_24),
                            contentDescription = "delete from number",
                        )
                    }
                }
            }
        }

    }

    @Preview(showBackground = true)
    @Composable
    fun DialerUI() {
        Surface(color = MaterialTheme.colorScheme.background) {
            PhoneWithNumberDisplayAndDial()
        }
    }

    private fun makeCall(number: String) {
        if (checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            val s = Uri.encode(number)

            telecomManager.placeCall(Uri.parse("tel:$s"), null)
        } else {
            requestPermissions(this, arrayOf(CALL_PHONE), REQUEST_PERMISSION)
        }
    }

    private fun offerReplacingDefaultDialer() {
        val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager

        activityResultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
    }

    companion object {
        const val REQUEST_PERMISSION = 0
    }
}