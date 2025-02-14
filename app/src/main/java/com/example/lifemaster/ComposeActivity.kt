package com.example.lifemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.lifemaster.ui.theme.LifeMaster_XMLTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeMaster_XMLTheme {
                PracticingCompose()
            }
        }
    }
}

@Composable
fun PracticingCompose(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
//    Scaffold {
//        DisposableEffect(lifecycleOwner) {
//            val observer = LifecycleEventObserver { _, event ->
//                when(event) {
//                    Lifecycle.Event.ON_START -> {}
//                    Lifecycle.Event.ON_STOP -> {}
//                    Lifecycle.Event.ON_PAUSE -> {}
//                    Lifecycle.Event.ON_STOP -> {}
//                    else -> {}
//                }
//            }
//            lifecycleOwner.lifecycle.addObserver(observer)
//            onDispose {
//                lifecycleOwner.lifecycle.removeObserver(observer)
//            }
//        }
//    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LifeMaster_XMLTheme {
        PracticingCompose()
    }
}