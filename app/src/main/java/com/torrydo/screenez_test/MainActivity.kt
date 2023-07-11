package com.torrydo.screenez_test

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.torrydo.screenez.ScreenEz
import com.torrydo.screenez_test.ui.theme.Screenez_testTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenEz.with(applicationContext)

        val screenWidth = ScreenEz.fullWidth
        val screenHeight = ScreenEz.fullHeight

        val statusBarHeight = ScreenEz.statusBarHeight
        val navBarHeight = ScreenEz.navBarHeight

        val safeWidth = ScreenEz.safeWidth
        val safeHeight = ScreenEz.safeHeight

        Log.d("<>", "-----------------------------: ");
        Log.d("<>", "screen width: $screenWidth");
        Log.d("<>", "screen height: $screenHeight");
        Log.d("<>", "statusbar height: $statusBarHeight");
        Log.d("<>", "navbar height: $navBarHeight");
        Log.d("<>", "safeWidth: $safeWidth");
        Log.d("<>", "safeHeight: $safeHeight");

        setContent {
            Screenez_testTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(136.toDp().dp)
                                .background(Color.White),
                        )
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenEz.refresh()

    }
}

internal fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
internal fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Screenez_testTheme {
        Greeting("Android")
    }
}