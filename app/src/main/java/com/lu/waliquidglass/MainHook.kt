package com.lu.waliquidglass
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.whatsapp") return
        val targets = listOf("com.whatsapp.Main", "com.whatsapp.HomeActivity")
        for (target in targets) {
            try {
                findAndHookMethod(target, lpparam.classLoader, "onCreate", Bundle::class.java,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val act = param.thisObject as Activity
                            act.window.decorView.postDelayed({ setupLiquidBar(act) }, 800)
                            act.window.decorView.postDelayed({ setupLiquidBar(act) }, 2000)
                        }
                    })
            } catch (e: Throwable) {}
        }
    }
    private var alreadyDone = false
    fun setupLiquidBar(activity: Activity) {
        if (alreadyDone) return
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val bottomNav = findBottomNav(root) ?: return
        alreadyDone = true
        val params = bottomNav.layoutParams as ViewGroup.MarginLayoutParams
        params.leftMargin = dp(activity, 18)
        params.rightMargin = dp(activity, 18)
        params.bottomMargin = dp(activity, 18)
        bottomNav.layoutParams = params
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(activity, 30).toFloat()
            setColor(Color.parseColor("#B32A2A2A"))
            setStroke(dp(activity, 1), Color.parseColor("#66FFFFFF"))
        }
        bottomNav.background = bg
        bottomNav.elevation = dp(activity, 10).toFloat()
        bottomNav.setOnTouchListener { v, _ ->
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
            v.postDelayed({
                v.animate().scaleX(1f).scaleY(1f).setDuration(300)
                    .setInterpolator(OvershootInterpolator(2.5f)).start()
            }, 100)
            false
        }
    }
    fun findBottomNav(view: View): View? {
        if (view.javaClass.name.contains("BottomNavigationView")) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findBottomNav(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }
    fun dp(act: Activity, v: Int): Int = (v * act.resources.displayMetrics.density).toInt()
}
