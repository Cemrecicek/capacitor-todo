package com.cemre.capacitortodo

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.getcapacitor.BridgeActivity
import org.json.JSONObject

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = bridge.webView
        val oldParent = webView.parent as? ViewGroup
        oldParent?.removeView(webView)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val nativeHeader = ComposeView(this).apply {
            setContent {
                NativeHeader(
                    onSendName = { name ->
                        sendNameToWeb(name)
                    }
                )
            }
        }

        rootLayout.addView(
            nativeHeader,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        rootLayout.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(rootLayout)
    }

    private fun sendNameToWeb(name: String) {
        val safeName = JSONObject.quote(name)

        bridge.webView.post {
            bridge.webView.evaluateJavascript(
                """
                window.dispatchEvent(
                  new CustomEvent("nativeUserName", {
                    detail: { userName: $safeName }
                  })
                );
                """.trimIndent(),
                null
            )
        }
    }
}

@Composable
fun NativeHeader(
    onSendName: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0F2FE))
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Native Alan",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text("İsminizi giriniz")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = Color(0xFF14B8A6),
                    unfocusedBorderColor = Color(0xFF94A3B8),
                    cursorColor = Color(0xFF14B8A6)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onSendName(name.trim())
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF14B8A6),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                )
            ) {
                Text("React tarafına gönder")
            }
        }
    }
}