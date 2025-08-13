package `is`.xyz.mpv
import android.content.Context
import android.util.Log
import `is`.xyz.mpv.MPVLib // 确保这个 import 不会报错

/**
 * 执行一次对 MPVLib 核心功能的无 UI 健全性检查。
 *
 * 这个方法会依次尝试：
 * 1. 检查 MPVLib 类是否存在。
 * 2. 创建 MPV 实例。
 * 3. 设置最基础的配置选项。
 * 4. 初始化 MPV 引擎。
 * 5. 测试发送命令和获取属性。
 * 6. 最终安全地销毁 MPV 实例。
 *
 * @param context 一个 Android Context，MPVLib 初始化时需要。
 */
fun testMpvLibCoreFunctionality(context: Context) {
    val TAG = "MPV_LIFECYCLE_TEST"
    var isMpvCreated = false // 标志位，用于确保只在创建成功后才销毁

    try {
        // --- 阶段 1: 类加载测试 (如果这里崩溃，说明 AAR/JAR 依赖没打包进来) ---
        Log.d(TAG, "阶段 1: 开始测试，检查 MPVLib 类...")
        // 这行代码本身就是个测试，如果类找不到，会直接抛出 NoClassDefFoundError
        val mpvClass = MPVLib::class.java
        Log.i(TAG, "成功: MPVLib 类已成功加载: ${mpvClass.name}")


        // --- 阶段 2: 创建原生实例 (如果这里崩溃，通常是 .so 文件问题) ---
        Log.d(TAG, "阶段 2: 尝试创建 MPV 实例 (MPVLib.create)...")
        MPVLib.create(context)
        isMpvCreated = true // 标记实例已创建
        Log.i(TAG, "成功: MPV 实例已创建。")


        // --- 阶段 3: 设置核心选项 ---
        Log.d(TAG, "阶段 3: 设置最基础的配置选项...")
        // 这些是让 MPV 在 Android 上运行所需的最少选项
        MPVLib.setOptionString("profile", "fast")
        MPVLib.setOptionString("gpu-context", "android")
        MPVLib.setOptionString("vo", "gpu") // 视频输出
        MPVLib.setOptionString("ao", "audiotrack,opensles") // 音频输出
        // 设置一个可写的配置目录
        MPVLib.setOptionString("config-dir", context.filesDir.path + "/mpv_config")
        Log.i(TAG, "成功: 基础选项已设置。")


        // --- 阶段 4: 初始化引擎 ---
        Log.d(TAG, "阶段 4: 初始化 MPV 引擎 (MPVLib.init)...")
        MPVLib.init()
        Log.i(TAG, "成功: MPV 引擎已初始化。")


        // --- 阶段 5: 双向通信测试 ---
        Log.d(TAG, "阶段 5: 测试属性获取 (getPropertyString)...")
        val mpvVersion = MPVLib.getPropertyString("mpv-version")
        if (mpvVersion.isNullOrEmpty()) {
            Log.e(TAG, "失败: 无法获取 mpv-version 属性！JNI 桥可能存在问题。")
        } else {
            Log.i(TAG, "成功: 获取到 mpv-version: $mpvVersion")
        }

        Log.d(TAG, "阶段 5: 测试发送命令 (command)...")
        // 发送一个简单的命令，比如设置音量
        MPVLib.command(arrayOf("set", "volume", "75"))
        Log.i(TAG, "成功: 'set volume' 命令已发送 (无崩溃)。")
        
        Log.d(TAG, "测试完成，所有核心功能似乎都正常工作！")

    } catch (e: UnsatisfiedLinkError) {
        Log.e(TAG, "致命错误: UnsatisfiedLinkError！", e)
        Log.e(TAG, "原因: 找不到 'libmpv.so' 文件或其依赖的库。请确认 .so 文件已正确打包到 APK 的 lib/<ABI>/ 目录下。")
    } catch (e: NoClassDefFoundError) {
        // 这个 catch 实际上可能在方法调用前就被触发
        Log.e(TAG, "致命错误: NoClassDefFoundError！", e)
        Log.e(TAG, "原因: 找不到 'is.xyz.mpv.MPVLib' 这个 Java/Kotlin 类。请确认包含这个类的 .aar 或 .jar 文件已作为 implementation 依赖被正确打包。")
    } catch (e: Exception) {
        // 捕获其他所有可能的异常，比如在 init 或 setOption 期间的内部错误
        Log.e(TAG, "测试期间发生未知异常！", e)
    } finally {
        // --- 阶段 6: 资源清理 ---
        if (isMpvCreated) {
            Log.d(TAG, "阶段 6: 销毁 MPV 实例 (MPVLib.destroy)...")
            MPVLib.destroy()
            Log.i(TAG, "成功: MPV 实例已销毁。")
        } else {
            Log.d(TAG, "阶段 6: MPV 实例未被创建，无需销毁。")
        }
    }
}