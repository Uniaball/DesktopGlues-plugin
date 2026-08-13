package com.fcl.plugin.desktopglues.settings

/**
 * 这台机器跑的是不是 MIUI / HyperOS。
 *
 * 用来挑默认皮肤：小米自家系统上，Miuix 皮肤和系统本身长得一样，那是更合适的第一印象。
 *
 * 判断的是**系统**，不是厂商。`Build.MANUFACTURER == "Xiaomi"` 看起来最省事，但它是错的：
 * 小米的机器刷上 GSI 或第三方 ROM 之后，硬件还是小米，系统已经是原生 Android，那时候
 * 端出 Miuix 皮肤只会显得格格不入。开发中用的那台 TrebleDroid 就是这个情形——机器是
 * 小米的，`ro.miui.*` 与 `ro.mi.os.*` 全空。
 *
 * 所以只认系统自己留下的痕迹，任一命中即可：
 *
 *  - `miui.os.Build` 这个类。MIUI 和 HyperOS 都把它放在 boot classpath 里，原生 Android
 *    没有。用 Class.forName 探测，不碰任何隐藏 API。
 *  - `ro.miui.ui.version.name`：MIUI 一直在设，HyperOS 多数版本仍然保留。
 *  - `ro.mi.os.version.name`：HyperOS 独有，用来兜住那些不再设上一条的版本。
 *
 * 属性读取走反射调用 android.os.SystemProperties.get。它不是公开 API，所以整段包在
 * try 里：读不到就当作「不是」，最坏的结果是默认皮肤仍为 Material——一个能用的结果，
 * 不是一次崩溃。
 */
object SystemUi {

    val isMiuiOrHyperOs: Boolean by lazy {
        hasMiuiClass() || !systemProperty("ro.miui.ui.version.name").isNullOrEmpty() ||
            !systemProperty("ro.mi.os.version.name").isNullOrEmpty()
    }

    private fun hasMiuiClass(): Boolean = try {
        Class.forName("miui.os.Build")
        true
    } catch (_: Throwable) {
        false
    }

    private fun systemProperty(name: String): String? = try {
        val clazz = Class.forName("android.os.SystemProperties")
        clazz.getMethod("get", String::class.java).invoke(null, name) as? String
    } catch (_: Throwable) {
        null
    }
}
