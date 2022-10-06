package com.redisfront.commons.util

import cn.hutool.core.swing.DesktopUtil
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONUtil
import com.redisfront.RedisFrontApplication
import com.redisfront.commons.constant.Const
import com.redisfront.commons.func.Fn
import javax.swing.JOptionPane

/**
 *  Upgrade
 *  @author Jin
 */
open class UpgradeUtils {
    companion object {
        private const val checkUrl = "https://gitee.com/westboy/RedisFront/raw/master/assets/version.json"
        private const val releaseUrl = "https://gitee.com/westboy/RedisFront/releases/"

        @JvmStatic
        fun checkVersion() {
            val currentVersion = Const.APP_VERSION
            if (currentVersion.startsWith("@")) {
                return
            }

            val httpRequest = HttpUtil.createGet(checkUrl)
            val httpResponse = httpRequest.execute()

            if (httpResponse.isOk) {

                val body = httpResponse.body()
                val versionObject = JSONUtil.parseObj(body)
                val newVersion = versionObject.getStr("version")
                if (Fn.equal(currentVersion, newVersion)) {
                    return
                }

                val currentVersionArray = currentVersion.split(".")
                val newVersionArray = newVersion.split(".")

                if (currentVersion.length == newVersion.length) {
                    val xIsTrue = Fn.equal(currentVersionArray[0], newVersionArray[0])
                    val yIsTrue = Fn.equal(currentVersionArray[1], newVersionArray[1])
                    val zIsTrue = Fn.equal(currentVersionArray[2], newVersionArray[2])
                    if (xIsTrue && yIsTrue && zIsTrue) {
                        return
                    }
                }

                val value = JOptionPane.showConfirmDialog(
                    RedisFrontApplication.frame,
                    "检测到有新版本，赶快快去看吧?",
                    "升级提醒",
                    JOptionPane.OK_CANCEL_OPTION
                )
                if (Fn.equal(value, JOptionPane.OK_OPTION)) {
                    DesktopUtil.browse(releaseUrl)
                }

            }
        }
    }
}
