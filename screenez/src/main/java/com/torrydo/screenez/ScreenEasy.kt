package com.torrydo.screenez

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.util.Size
import java.lang.ref.WeakReference

class ScreenEasy {

    private var _screenInfo: ScreenInfoApi? = null
    private val screenInfo: ScreenInfoApi
        get() {
            if (_screenInfo == null) {
                throw IllegalStateException("You haven't call `with(context)`")
            }
            return _screenInfo!!
        }


    private var _weakContext: WeakReference<Context>? = null
    private val context: Context
        get() {
            if (_weakContext == null) {
                throw IllegalStateException("You haven't call `with(context)`")
            }
            return _weakContext!!.get()!!
        }


    //region Public API ----------------------------------------------------------------------------

    /**
     * call this function before any other function to setup properly
     *
     * just need to call one time, calling multiple times is okay, but unnecessary
     * */
    fun with(context: Context) {
        _weakContext = WeakReference(context.applicationContext)
        refresh()
    }

    /**
     * refresh attributes, call when configuration change, rotation, ...
     * */
    fun refresh() {
        _screenInfo = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> ApiLevel30(context)  // Android 11+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> ApiLevel29(context)  // Android 10
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> ApiLevel28(context)  // Android 9
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 -> ApiLevel25(context) // Android 7.1, 8, 8.1
            else -> ApiLevel23(context)  // Android 5, 6, 7
        }

//        Log.d("<>", "==========================: ");
//        Log.d("<>", "fullSize: $fullSize");
//        Log.d("<>", "statusBar: $statusBarPadding");
//        Log.d("<>", "navBar: $navBarPadding");
//        Log.d("<>", "displayCutouts: $cutoutPadding");
//        Log.d("<>", "safeSize: $safeSize");
//        Log.d("<>", "safePadding: $safeScreenPadding");
//        Log.d("<>", "rotation: ${screenInfo.screenRotation()}");
    }

    /**
     * represent screen padding of system bar for navigation
     *
     * i.e: if navigation bar is on the bottom (left=0, top=0, right=0, bottom=100)
     * */
    val navBarPadding: ScreenPadding get() = screenInfo.navBarPadding()

    /**
     * represent screen padding of status bar
     *
     * i.e: if status bar is on the top (left=0, top=100, right=0, bottom=0)
     * */
    val statusBarPadding: ScreenPadding get() = screenInfo.statusPadding()

    /**
     * Represent screen padding of cutout for the notch, or similar.
     *
     * The cutout appeared from android 9 (api 28)
     *
     * i.e: if the cutout of the notch is on the top (left=0, top=100, right=0, bottom=0)
     * */
    val cutoutPadding: ScreenPadding get() = screenInfo.cutoutPadding()

    /**
     * return full width of the screen in pixel.
     * */
    val fullWidth get() = fullSize.width

    /**
     * return full height of the screen in pixel.
     * */
    val fullHeight get() = fullSize.height

    /**
     * return full size of the screen in pixel.
     * */
    val fullSize get() = screenInfo.screenSize()


    /**
     * return true if buttons navigation is being used (ie: 3 buttons: back, home, recent apps)
     * */
    fun isButtonsNavigation() = screenInfo.isButtonsNavigation()

    /**
     * return true if gesture navigation is being used (ie: line gesture, no-line gesture)
     * */
    fun isGestureNavigation() = screenInfo.isGestureNavigation()

    /**
     * The safeArea is the area of the screen that is not obscured by the navigation bar, status bar, or other system UI elements.
     *
     * ie: topLeft, topRight, bottomRight, bottomLeft: [(0, 48), (1440, 48), (1440, 2340), (0, 2340)
     * */
    val safeArea: ScreenArea
        get() {
            val paddings = safeScreenPadding

            val topLeft = Point(paddings.left, paddings.top)
            val topRight = Point(fullWidth - paddings.right, paddings.top)
            val bottomRight =
                Point(fullWidth - paddings.right, fullHeight - paddings.bottom)
            val bottomLeft = Point(paddings.left, fullHeight - paddings.bottom)

            return ScreenArea(
                topLeft = topLeft,
                topRight = topRight,
                bottomRight = bottomRight,
                bottomLeft = bottomLeft
            )
        }

    /**
     * Returns the safe screen padding.
     *
     * The safe screen padding is the amount of padding that should be applied to the
     * edges of the screen to avoid overlapping with the navigation bar, status bar,
     * or other system UI elements.
     *
     * @return A `ScreenPadding` object, which is a tuple of four integers representing
     * the padding on the left, top, right, and bottom sides of the screen.
     */
    val safeScreenPadding: ScreenPadding
        get() {
            val topPadding = safePaddingTop
            val bottomPadding = safePaddingBottom
            val leftPadding = safePaddingLeft
            val rightPadding = safePaddingRight

            return ScreenPadding(
                left = leftPadding,
                top = topPadding,
                right = rightPadding,
                bottom = bottomPadding,
            )
        }

    /**
     * The safe padding is the amount of padding that should be applied to the
     * left edge of the screen to avoid overlapping with the navigation bar, status bar,
     * or other system UI elements.
     *
     * @return Integer, the padding on the left side of the screen.
     */
    val safePaddingLeft: Int
        get() {
            return maxOf(
                screenInfo.cutoutPadding().left,
                screenInfo.statusPadding().left,
                screenInfo.navBarPadding().left
            )
        }

    /**
     * The safe padding is the amount of padding that should be applied to the
     * right edge of the screen to avoid overlapping with the navigation bar, status bar,
     * or other system UI elements.
     *
     * @return Integer, the padding on the right side of the screen.
     */
    val safePaddingRight: Int
        get() {
            return maxOf(
                screenInfo.cutoutPadding().right,
                screenInfo.statusPadding().right,
                screenInfo.navBarPadding().right
            )
        }

    /**
     * The safe padding is the amount of padding that should be applied to the
     * top edge of the screen to avoid overlapping with the navigation bar, status bar,
     * or other system UI elements.
     *
     * @return Integer, the padding on the top side of the screen.
     */
    val safePaddingTop: Int
        get() {
            return maxOf(
                screenInfo.cutoutPadding().top,
                screenInfo.statusPadding().top,
                screenInfo.navBarPadding().top
            )
        }

    /**
     * The safe padding is the amount of padding that should be applied to the
     * bottom edge of the screen to avoid overlapping with the navigation bar, status bar,
     * or other system UI elements.
     *
     * @return Integer, the padding on the bottom side of the screen.
     */
    val safePaddingBottom: Int
        get() {
            return maxOf(
                screenInfo.cutoutPadding().bottom,
                screenInfo.statusPadding().bottom,
                screenInfo.navBarPadding().bottom
            )
        }


    /**
     * The safe width is the width of the screen after subtracting the system bar on the left and right sides
     *
     * Returns: Integer (pixel unit), the safe width of the screen.
     */
    val safeWidth: Int
        get() {
            val total = safePaddingLeft + safePaddingRight
            return fullWidth - total
        }

    /**
     * The safe height is the height of the screen after subtracting the system bar on the top and bottom sides
     *
     * Returns: Integer (pixel unit), the safe height of the screen.
     */
    val safeHeight: Int
        get() {
            val bottomGestureNavHeight = navBarPadding.height()
            val topStatusBarHeight = statusBarPadding.height()

            val total = bottomGestureNavHeight + topStatusBarHeight

            return fullHeight - total
        }

    /**
     * The safe size is the Size(width, height) of the screen after subtracting the system bar on all sides
     *
     * Returns: Integer (pixel unit), the safe height of the screen.
     */
    val safeSize get() = Size(safeWidth, safeHeight)

    /**
     * @return status bar height (pixel unit)
     * */
    val statusBarHeight: Int get() = statusBarPadding.top

    /**
     * @return navigation bar height (both gesture and buttons in pixel unit)
     * */
    val navBarHeight: Int get() = screenInfo.navBarHeight()


    /**
     * @return true if screen is in portrait mode
     * */
    fun isPortrait(): Boolean {
        return screenInfo.orientation() == Configuration.ORIENTATION_PORTRAIT
    }


    /**
     * The screen rotation is the orientation of the screen.
     *
     * ie: PORTRAIT, LANDSCAPE, REVERSED_PORTRAIT, REVERSED_LANDSCAPE
     *
     * Returns: ScreenRotation
     */
    val screenRotation: ScreenRotation
        get() {
            return screenInfo.screenRotation()
        }

    //endregion ------------------------------------------------------------------------------------


}