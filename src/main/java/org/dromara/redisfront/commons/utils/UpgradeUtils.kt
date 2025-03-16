package org.dromara.redisfront.commons.utils

import cn.hutool.core.swing.DesktopUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONUtil
import javax.swing.JOptionPane

/**
 *  Upgrade
 *  @author Jin
 */
open class UpgradeUtils {
    companion object {
        private const val GITEE_URL = "https://gitee.com/dromara/RedisFront/raw/master/assets/version.json"

        @JvmStatic
        fun checkVersion(currentVersion: String) {

            val httpRequest = HttpUtil.createGet(GITEE_URL)
            val httpResponse = httpRequest.execute()

            if (httpResponse.isOk) {
                val body = httpResponse.body()
                val versionObject = JSONUtil.parseObj(body)
                val newVersion = versionObject.getStr("version")
                if (StrUtil.compareVersion(newVersion, currentVersion) == 0) {
                    return
                }

                val downloadUrl =
                    versionObject.getStr("downloadUrl") ?: "https://gitee.com/dromara/RedisFront/releases/"

                val confirmDialog =
                    JOptionPane.showConfirmDialog(null, "发现新版本，是否下载？", "发现新版本", JOptionPane.YES_NO_OPTION)

                if (confirmDialog != JOptionPane.YES_OPTION) {
                    return
                }

                DesktopUtil.browse(downloadUrl)
            }
        }
    }
}
