package com.torrydo.screenez

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.graphics.Insets
import androidx.window.core.ExperimentalWindowApi
import androidx.window.layout.WindowMetricsCalculator


// base api from android 6 (api 23)
internal open class ApiLevel23(private val context: Context) : ScreenInfoApi {

    override fun screenSize(): Size {
        val wmc = WindowMetricsCalculator.getOrCreate()
        val maxWindowMetrics = wmc.computeMaximumWindowMetrics(context).bounds
        return Size(maxWindowMetrics.width(), maxWindowMetrics.height())
    }

    override fun orientation(): Int {
        return context.resources.configuration.orientation
    }

    override fun screenRotation(): ScreenRotation {

        val rotation = getDisplay()?.rotation

        val screenSize = screenSize()
        val width = screenSize.width
        val height = screenSize.height

        val isPortrait = (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
        val isLandscape = (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)

        return if (isPortrait && height > width || isLandscape && width > height) { // if natural orientation is portrait
            when (rotation) {
                Surface.ROTATION_0 -> ScreenRotation.PORTRAIT
                Surface.ROTATION_90 -> ScreenRotation.REVERSED_LANDSCAPE // weird? Idk why ROTATION_90 appear when the device rotate to the LEFT
                Surface.ROTATION_180 -> ScreenRotation.REVERSED_PORTRAIT
                Surface.ROTATION_270 -> ScreenRotation.LANDSCAPE
                else -> ScreenRotation.PORTRAIT
            }
        } else { // natural orientation is landscape
            when (rotation) {
                Surface.ROTATION_0 -> ScreenRotation.LANDSCAPE
                Surface.ROTATION_90 -> ScreenRotation.PORTRAIT
                Surface.ROTATION_180 -> ScreenRotation.REVERSED_LANDSCAPE
                Surface.ROTATION_270 -> ScreenRotation.REVERSED_PORTRAIT
                else -> ScreenRotation.LANDSCAPE
            }
        }

    }

    override fun statusPadding(): ScreenPadding {
        return ScreenPadding.None.copy(top = statusBarHeight())
    }

    override fun navBarPadding(): ScreenPadding {

        val height = navBarHeight()

        val nonePadding = ScreenPadding.None

        return when (orientation()) {
            Configuration.ORIENTATION_PORTRAIT -> nonePadding.copy(bottom = height)
            Configuration.ORIENTATION_LANDSCAPE -> nonePadding.copy(right = height)

            else -> nonePadding
        }
    }

    // until android 9, fortunately no cutout
    override fun cutoutPadding(): ScreenPadding {
        return ScreenPadding.None
    }

    override fun isButtonsNavigation(): Boolean {
        return navigationMode() == 0
    }

    override fun isGestureNavigation(): Boolean {
        return navigationMode() == 2
    }

    override fun navBarHeight(): Int {
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    override fun statusBarHeight(): Int {
        val statusBarHeightId =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(statusBarHeightId)  // check android 11+
    }

    // other

    open fun getDisplay(): Display? {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay
    }

    /**
     * - 0: three-button mode
     * - 2: gesture mode
     * */
    private fun navigationMode(): Int {
        return try {
            // android <=9 will be crashed.
            Settings.Secure.getInt(
                this.context.contentResolver,
                "navigation_mode"
            )
        } catch (e: Exception) {
            0
        }
    }
}

// api level 30+ (android 11+)
@OptIn(ExperimentalWindowApi::class)
@RequiresApi(Build.VERSION_CODES.R)
internal class ApiLevel30(private val context: Context) : ApiLevel29(context) {

    private var screenSize: Size = Size(0, 0)

    private var navBarInsets: Insets = Insets.NONE
    private var statusBarInsets: Insets = Insets.NONE
    private var cutoutInsets: Insets = Insets.NONE
    private var navBarHeight: Int = 0
    private var statusBarHeight : Int = 0


    init {

        screenSize = screenSize()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowMetrics = windowManager.currentWindowMetrics
        val windowInsets = windowMetrics.windowInsets

        navBarInsets = windowInsets.getInsets(
            WindowInsets.Type.navigationBars()
        ).toInsetsCompat()

        statusBarInsets = windowInsets.getInsets(
            WindowInsets.Type.statusBars()
        ).toInsetsCompat()

        cutoutInsets = windowInsets.getInsets(
            WindowInsets.Type.displayCutout()
        ).toInsetsCompat()


        val insets = windowMetrics.windowInsets

        statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top
        navBarHeight = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
    }

    private fun android.graphics.Insets.toInsetsCompat(): Insets {
        return Insets.of(this.left, this.top, this.right, this.bottom)
    }

    override fun navBarPadding(): ScreenPadding {
        return ScreenPadding.fromInsets(navBarInsets)
    }

    override fun statusPadding(): ScreenPadding {
        return ScreenPadding.fromInsets(statusBarInsets)
    }

    override fun cutoutPadding(): ScreenPadding {
        return ScreenPadding.fromInsets(cutoutInsets)
    }

    override fun navBarHeight(): Int {
        return navBarHeight
    }

    override fun statusBarHeight(): Int {
        return statusBarHeight
    }
}

// api level 29 (android 10)
@RequiresApi(AndroidVersions.`10`)
internal open class ApiLevel29(private val context: Context) : ApiLevel28(context) {

    override fun cutoutPadding(): ScreenPadding {
        val nonePadding = ScreenPadding.None

        val wm = context.getSystemService(WindowManager::class.java)

        val cutout = wm.defaultDisplay.cutout ?: return nonePadding

        return ScreenPadding(
            left = cutout.safeInsetLeft,
            top = cutout.safeInsetTop,
            right = cutout.safeInsetRight,
            bottom = cutout.safeInsetBottom
        )
    }

}

// api level 28 (android 9)
@RequiresApi(AndroidVersions.`9`)
internal open class ApiLevel28(private val context: Context) : ApiLevel25(context) {


    // from android 9, gesture navigation appear,
    override fun navBarPadding(): ScreenPadding {
        if (isGestureNavigation() && orientation() == Configuration.ORIENTATION_LANDSCAPE) {
            return ScreenPadding.None.copy(bottom = navBarHeight())
        }
        return super.navBarPadding()
    }

    // Unfortunately, I can't find "getCutout()" method for android 9 yet (without using activity context)
    override fun cutoutPadding(): ScreenPadding {

//        val wmc = WindowMetricsCalculator.getOrCreate()
//        val maxWindowMetrics = wmc.computeMaximumWindowMetrics(this.context)
//        val cutoutInsets = maxWindowMetrics.getWindowInsets().getInsets(WindowInsetsCompat.Type.displayCutout())

        // doesn't work, cutout insets return 0,0,0,0
        return super.cutoutPadding()
    }

}

// api level 25 (android 7.1)
@RequiresApi(AndroidVersions.`7_1`)
internal open class ApiLevel25(private val context: Context) : ApiLevel23(context) {

    // starting with Android 7.1 the nav bar may also be located on the LEFT side of the screen
    // On Huawei P30 pro , when Surface.ROTATION_90 and Surface.ROTATION_270 , navigation bar on the right.
    override fun navBarPadding(): ScreenPadding {
        val height = navBarHeight()

        val nonePadding = ScreenPadding.None

        return when (screenRotation()) {
            ScreenRotation.PORTRAIT -> nonePadding.copy(bottom = height)
            ScreenRotation.REVERSED_PORTRAIT -> nonePadding.copy(bottom = height)
            ScreenRotation.LANDSCAPE -> nonePadding.copy(left = height)
            ScreenRotation.REVERSED_LANDSCAPE -> nonePadding.copy(right = height)

            else -> nonePadding
        }
    }

}
