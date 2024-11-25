// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.virogu.constant.Constant
import java.util.prefs.Preferences

private val preferences = Preferences.userRoot()
private const val KEY_LAST_WINDOWS_SIZE = "key-last-windows-size"

private val size by lazy {
//    val lastSize = preferences.get(KEY_LAST_WINDOWS_SIZE, "")
//    try {
//        if (lastSize.isNullOrEmpty()) {
//            throw IllegalArgumentException("lastSize is Null Or Empty")
//        }
//        val w = lastSize.split("*")[0].toFloat()
//        val h = lastSize.split("*")[1].toFloat()
//        DpSize(w.dp, h.dp)
//    } catch (e: Throwable) {
//        DpSize(700.dp, 620.dp)
//    }
    DpSize(700.dp, 620.dp)
}

@Composable
fun KeyDialog(onKeySet: (String) -> Unit, onDismiss : () -> Unit) {
    var key by remember { mutableStateOf(Constant.PRI_KEY) }
    var isKeyValid by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val savedKey = Constant.getPrivateKey()
        if (savedKey.isNotEmpty()) {
            key = savedKey
        }
        isKeyValid = key.matches(Regex("[a-zA-Z0-9]{64}"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color.White,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(500.dp, 270.dp).padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "请输入密钥",
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(fontSize = MaterialTheme.typography.h6.fontSize)
                )
                TextField(
                    value = key,
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex("[a-zA-Z0-9]*")) && newValue.length <= 64) {
                            key = newValue
                            isKeyValid = newValue.length == 64
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(
                        start = 15.dp,
                        end = 15.dp,
                        bottom = 16.dp
                    ),
                    textStyle = TextStyle(fontSize = MaterialTheme.typography.h6.fontSize),
                    maxLines = 2,
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        Constant.setPrivateKey(key)
                        onKeySet(key)
                        onDismiss()
                    },
                    enabled = isKeyValid,
                ) {
                    Text("确定")
                }
            }
        }
    }
}

fun main() = application {
    val icon = painterResource("icon.ico")
    val state = rememberWindowState(
        placement = WindowPlacement.Floating,
        size = size,
        position = WindowPosition.Aligned(Alignment.Center),
    )
    var showKeyDialog by remember { mutableStateOf(false) }
    Window(
        onCloseRequest = ::exitApplication,
        title = "XLog解密工具",
        state = state,
        undecorated = false,
        icon = icon,
    ) {
        Tray(icon = icon, menu = {
            if (state.isMinimized) {
                Item("显示主窗口", onClick = {
                    state.isMinimized = false
                })
            } else {
                Item("隐藏主窗口", onClick = {
                    state.isMinimized = true
                })
            }
            Item("设置密钥", onClick = {
                println("set private key clicked")
                showKeyDialog = true
            })
            Item("退出", onClick = ::exitApplication)
        })
        if (showKeyDialog) {
            KeyDialog(
                onKeySet = { key ->
                    // 处理密钥
                    println("private key: $key")
                    showKeyDialog = false
                },
                onDismiss = {
                    showKeyDialog = false
                }
            )
        }
        App(window, this@application, state)
    }
}
