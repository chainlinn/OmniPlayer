package `is`.xyz.mpv // 保持包名一致，以便访问 internal/private 成员（如果需要）

import android.content.Context
import android.util.Log

/**
 * 这是一个极简化的工具类，专门用于在运行时测试 `MPVLib` 是否被正确加载和初始化。
 * 它不包含任何 UI 逻辑，只关注与 `MPVLib` 的基础交互。
 */
object MPVTestKit {

    private const val TAG = "MPVTestKit"
    private var isMpvInitialized = false

    /**
     * 核心初始化方法。
     * 尝试创建 MPV 实例并设置最基础的选项。
     * 如果 `MPVLib` 类不存在或初始化失败，此方法会捕获异常并记录日志。
     *
     * @param context Android 应用上下文。
     * @return 如果成功初始化，返回 true；否则返回 false。
     */
    fun initialize(context: Context): Boolean {
        if (isMpvInitialized) {
            Log.d(TAG, "MPVLib 已经初始化过了。")
            return true
        }

        try {
            // **第一道关卡：检查 MPVLib 类是否存在**
            // 如果 `MPVLib` 类找不到，这里就会直接抛出 NoClassDefFoundError
            Log.d(TAG, "正在尝试访问 MPVLib 类...")
            val mpvClass = MPVLib::class.java
            Log.d(TAG, "成功访问到 MPVLib 类: ${mpvClass.name}")

            // **第二道关卡：调用 MPVLib 的静态方法来创建实例**
            // 这是与原生库交互的第一步。
            Log.d(TAG, "正在调用 MPVLib.create()")
            MPVLib.create(context)
            Log.d(TAG, "MPVLib.create() 调用成功。")
            
            // **第三道关卡：设置一个最基础的选项，验证通信**
            // 这确保了我们不仅能创建实例，还能向其发送指令。
            Log.d(TAG, "正在设置日志等级选项...")
            MPVLib.setOptionString("log-file", "no") // 简单地禁用日志文件，作为测试
            Log.d(TAG, "设置选项成功。")

            // **第四道关卡：初始化播放器核心**
            Log.d(TAG, "正在调用 MPVLib.init()")
            MPVLib.init()
            Log.d(TAG, "MPVLib.init() 调用成功。")
            
            isMpvInitialized = true
            Log.i(TAG, "MPVLib 初始化成功！")
            return true

        } catch (e: UnsatisfiedLinkError) {
            // 这个错误意味着 .so 文件没有找到或无法加载
            Log.e(TAG, "初始化失败：找不到或无法链接 .so 原生库。请检查 jniLibs 目录。", e)
            return false
        } catch (e: NoClassDefFoundError) {
            // 这个错误意味着 MPVLib.java/kt 这个 "胶水代码" 的类文件没有被打包进 APK
            Log.e(TAG, "初始化失败：找不到 MPVLib 类。请检查 .aar/.jar 依赖是否正确。", e)
            return false
        } catch (e: Exception) {
            // 捕获其他所有可能的异常
            Log.e(TAG, "MPVLib 初始化过程中发生未知错误。", e)
            return false
        }
    }

    /**
     * 获取 MPV 核心版本号。
     * 这是一个简单的只读操作，用于验证初始化后的 MPV 实例是否能正常响应。
     *
     * @return 如果成功，返回版本号字符串；否则返回 null。
     */
    fun getMpvVersion(): String? {
        if (!isMpvInitialized) {
            Log.w(TAG, "无法获取版本号，因为 MPVLib 尚未初始化。")
            return null
        }
        return try {
            MPVLib.getPropertyString("mpv-version")
        } catch (e: Exception) {
            Log.e(TAG, "获取 mpv-version 失败。", e)
            null
        }
    }

    /**
     * 销毁 MPV 实例，释放资源。
     */
    fun destroy() {
        if (isMpvInitialized) {
            Log.d(TAG, "正在销毁 MPVLib 实例...")
            MPVLib.destroy()
            isMpvInitialized = false
            Log.i(TAG, "MPVLib 实例已销毁。")
        }
    }
}