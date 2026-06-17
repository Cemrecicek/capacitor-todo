package com.cemre.capacitortodo

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "ComposeBridge")
class ComposeBridgePlugin : Plugin() {

    private var composeView: ComposeView? = null

    @PluginMethod
    fun showCompose(call: PluginCall) {
        val x = call.getInt("x") ?: 0
        val y = call.getInt("y") ?: 0
        val width = call.getInt("width") ?: 0
        val height = call.getInt("height") ?: 0

        activity.runOnUiThread {

            try {
                val mainContainer = activity.findViewById<ViewGroup>(android.R.id.content)

                val params = FrameLayout.LayoutParams(width, height).apply {
                    leftMargin = x
                    topMargin = y
                }

                if (composeView == null) {
                    composeView = ComposeView(activity).apply {
                        setContent {
                            NativeComposeContent(
                                onSendUserName = { userName ->
                                    val data = JSObject()
                                    data.put("userName", userName)
                                    notifyListeners("nativeUserName", data)
                                })
                        }
                    }

                    mainContainer.addView(composeView, params)
                } else {
                    composeView?.layoutParams = params
                    composeView?.requestLayout()
                }
                call.resolve()
            } catch (e: Exception) {
                e.printStackTrace()
                call.reject("Native görünüm yüklenirken hata oluştu: ${e.localizedMessage}")
            }

            call.resolve()
        }
    }

    @PluginMethod
    fun hideNative(call: PluginCall) {
        activity.runOnUiThread {
            composeView?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                composeView = null
            }
            call.resolve()
        }
    }
}

@Composable
fun NativeComposeContent(
    onSendUserName: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0F2FE))
                .padding(16.dp)
        ) {
            Text(
                text = "Native Alan",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inputText, onValueChange = { inputText = it }, placeholder = {
                Text("İsminizi giriniz")
            }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onSendUserName(inputText.trim())
                    inputText = ""
                }, enabled = inputText.isNotBlank(), modifier = Modifier.fillMaxWidth()
            ) {
                Text("Capacitor tarafına gönder")
            }
        }
    }
}