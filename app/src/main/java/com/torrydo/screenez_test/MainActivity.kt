package com.torrydo.screenez_test

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.Surface
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torrydo.screenez.ScreenArea
import com.torrydo.screenez.ScreenEz
import com.torrydo.screenez.ScreenPadding
import com.torrydo.screenez_test.ui.theme.Screenez_testTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenEz.with(this)
        val comparisonRows = screenComparisonRows(directAndroidScreenData())

        setContent {
            Screenez_testTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    ScreenComparisonTable(comparisonRows)
                }
            }
        }
    }

    private fun directAndroidScreenData(): AndroidScreenData {
        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val fullSize = directFullSize(wm)
        val insets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.windowInsets
        } else {
            null
        }

        val statusPadding = insets?.toPadding(WindowInsets.Type.statusBars())
            ?: ScreenPadding.None.copy(top = systemDimension("status_bar_height"))
        val navPadding = insets?.toPadding(WindowInsets.Type.navigationBars())
            ?: ScreenPadding.None.copy(bottom = systemDimension("navigation_bar_height"))
        val cutoutPadding = insets?.toPadding(WindowInsets.Type.displayCutout())
            ?: ScreenPadding.None
        val safePadding = ScreenPadding(
            left = maxOf(statusPadding.left, navPadding.left, cutoutPadding.left),
            top = maxOf(statusPadding.top, navPadding.top, cutoutPadding.top),
            right = maxOf(statusPadding.right, navPadding.right, cutoutPadding.right),
            bottom = maxOf(statusPadding.bottom, navPadding.bottom, cutoutPadding.bottom)
        )

        return AndroidScreenData(
            fullSize = fullSize,
            statusPadding = statusPadding,
            navPadding = navPadding,
            cutoutPadding = cutoutPadding,
            safePadding = safePadding,
            safeSize = Size(
                fullSize.width - safePadding.left - safePadding.right,
                fullSize.height - safePadding.top - safePadding.bottom
            ),
            statusBarHeight = statusPadding.top,
            navBarHeight = navPadding.height(),
            rotation = directRotation(wm)
        )
    }

    @Suppress("DEPRECATION")
    private fun directFullSize(wm: WindowManager): Size {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.maximumWindowMetrics.bounds
            Size(bounds.width(), bounds.height())
        } else {
            val displayMetrics = DisplayMetrics().also { wm.defaultDisplay.getRealMetrics(it) }
            Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    @Suppress("DEPRECATION")
    private fun directRotation(wm: WindowManager): String {
        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.rotation
        } else {
            wm.defaultDisplay.rotation
        }

        return when (rotation) {
            Surface.ROTATION_0 -> "ROTATION_0"
            Surface.ROTATION_90 -> "ROTATION_90"
            Surface.ROTATION_180 -> "ROTATION_180"
            Surface.ROTATION_270 -> "ROTATION_270"
            else -> "UNKNOWN"
        }
    }

    private fun systemDimension(name: String): Int {
        val resourceId = resources.getIdentifier(name, "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenEz.refresh()

    }
}

private fun screenComparisonRows(androidData: AndroidScreenData): List<ScreenComparisonRow> {
    return listOf(
        ScreenComparisonRow("Full size", ScreenEz.fullSize.format(), androidData.fullSize.format()),
        ScreenComparisonRow("Full width", "${ScreenEz.fullWidth}px", "${androidData.fullSize.width}px"),
        ScreenComparisonRow("Full height", "${ScreenEz.fullHeight}px", "${androidData.fullSize.height}px"),
        ScreenComparisonRow("Status bar padding", ScreenEz.statusBarPadding.format(), androidData.statusPadding.format()),
        ScreenComparisonRow("Navigation bar padding", ScreenEz.navBarPadding.format(), androidData.navPadding.format()),
        ScreenComparisonRow("Cutout padding", ScreenEz.cutoutPadding.format(), androidData.cutoutPadding.format()),
        ScreenComparisonRow("Safe screen padding", ScreenEz.safeScreenPadding.format(), androidData.safePadding.format()),
        ScreenComparisonRow("Safe area", ScreenEz.safeArea.format(), androidData.safeArea.format()),
        ScreenComparisonRow("Safe size", ScreenEz.safeSize.format(), androidData.safeSize.format()),
        ScreenComparisonRow("Safe width", "${ScreenEz.safeWidth}px", "${androidData.safeSize.width}px"),
        ScreenComparisonRow("Safe height", "${ScreenEz.safeHeight}px", "${androidData.safeSize.height}px"),
        ScreenComparisonRow("Status bar height", "${ScreenEz.statusBarHeight}px", "${androidData.statusBarHeight}px"),
        ScreenComparisonRow("Navigation bar height", "${ScreenEz.navBarHeight}px", "${androidData.navBarHeight}px"),
        ScreenComparisonRow("Rotation", ScreenEz.screenRotation.name, androidData.rotation),
        ScreenComparisonRow("Portrait", ScreenEz.isPortrait().toString(), (androidData.fullSize.height >= androidData.fullSize.width).toString()),
        ScreenComparisonRow("Buttons navigation", ScreenEz.isButtonsNavigation().toString(), "N/A"),
        ScreenComparisonRow("Gesture navigation", ScreenEz.isGestureNavigation().toString(), "N/A")
    )
}

private data class AndroidScreenData(
    val fullSize: Size,
    val statusPadding: ScreenPadding,
    val navPadding: ScreenPadding,
    val cutoutPadding: ScreenPadding,
    val safePadding: ScreenPadding,
    val safeSize: Size,
    val statusBarHeight: Int,
    val navBarHeight: Int,
    val rotation: String
) {
    val safeArea: ScreenArea
        get() = ScreenArea(
            topLeft = android.graphics.Point(safePadding.left, safePadding.top),
            topRight = android.graphics.Point(fullSize.width - safePadding.right, safePadding.top),
            bottomRight = android.graphics.Point(
                fullSize.width - safePadding.right,
                fullSize.height - safePadding.bottom
            ),
            bottomLeft = android.graphics.Point(safePadding.left, fullSize.height - safePadding.bottom)
        )
}

private data class ScreenComparisonRow(
    val label: String,
    val screenEzValue: String,
    val androidApiValue: String
)

private fun WindowInsets.toPadding(typeMask: Int): ScreenPadding {
    val insets = getInsets(typeMask)
    return ScreenPadding(
        left = insets.left,
        top = insets.top,
        right = insets.right,
        bottom = insets.bottom
    )
}

private fun ScreenPadding.height(): Int = top + bottom

private fun Size.format(): String = "${width}px x ${height}px"

private fun ScreenPadding.format(): String {
    return "L: $left\nT: $top\nR: $right\nB: $bottom"
}

private fun ScreenArea.format(): String {
    return "TL: ${topLeft.format()}\nTR: ${topRight.format()}\nBR: ${bottomRight.format()}\nBL: ${bottomLeft.format()}"
}

private fun android.graphics.Point.format(): String = "($x,$y)"

private val AppBackground = Color(0xFF111315)
private val PanelBackground = Color(0xFF181B1F)
private val HeaderBackground = Color(0xFF232832)
private val RowBackground = Color(0xFF171A1E)
private val AlternateRowBackground = Color(0xFF1D2126)
private val BorderColor = Color(0xFF343A43)
private val PrimaryText = Color(0xFFF3F6FA)
private val SecondaryText = Color(0xFFA8B0BC)
private val AccentColor = Color(0xFF66D9C3)

internal fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
internal fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

@Composable
private fun ScreenComparisonTable(rows: List<ScreenComparisonRow>) {
    val safePadding = ScreenEz.safeScreenPadding

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                start = safePadding.left.toDp().dp,
                top = safePadding.top.toDp().dp,
                end = safePadding.right.toDp().dp,
                bottom = safePadding.bottom.toDp().dp
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Screen data comparison",
            color = PrimaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 28.sp
        )
        Text(
            text = "ScreenEz vs Android API",
            color = SecondaryText,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(AccentColor)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            TableHeaderCell(
                text = "ScreenEz data",
                modifier = Modifier.weight(1f)
            )
            TableHeaderCell(
                text = "Android API direct using",
                modifier = Modifier.weight(1f)
            )
        }

        rows.forEachIndexed { index, row ->
            val rowColor = if (index % 2 == 0) RowBackground else AlternateRowBackground
            Row(modifier = Modifier.fillMaxWidth()) {
                TableValueCell(
                    label = row.label,
                    value = row.screenEzValue,
                    backgroundColor = rowColor,
                    modifier = Modifier.weight(1f)
                )
                TableValueCell(
                    label = row.label,
                    value = row.androidApiValue,
                    backgroundColor = rowColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .border(0.5.dp, BorderColor)
            .background(HeaderBackground)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            color = PrimaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun TableValueCell(
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .border(0.5.dp, BorderColor)
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = SecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 13.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = value,
            color = PrimaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}

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
