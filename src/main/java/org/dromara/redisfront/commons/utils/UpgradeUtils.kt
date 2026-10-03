package org.dromara.redisfront.commons.utils

import cn.hutool.core.swing.DesktopUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONUtil
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 *  Upgrade
 *  @author Jin
 */
open class UpgradeUtils {
    companion object {
        private const val GITEE_URL = "https://gitee.com/dromara/RedisFront/raw/master/assets/version.json"
        private const val DEFAULT_DOWNLOAD_URL = "https://gitee.com/dromara/RedisFront/releases/"
        private const val HTTP_TIMEOUT_MILLIS = 5000

        @JvmStatic
        fun checkVersion(currentVersion: String) {

            val httpResponse = HttpUtil.createGet(GITEE_URL)
                .setConnectionTimeout(HTTP_TIMEOUT_MILLIS)
                .setReadTimeout(HTTP_TIMEOUT_MILLIS)
                .execute()

            if (!httpResponse.isOk) {
                return
            }

            val versionObject = JSONUtil.parseObj(httpResponse.body())
            val newVersion = versionObject.getStr("version") ?: return

            // 仅当远端版本号更大时才提示，避免运行中的开发版本被提示"升级"到旧版
            if (StrUtil.compareVersion(newVersion, currentVersion) <= 0) {
                return
            }

            val downloadUrl = versionObject.getStr("downloadUrl") ?: DEFAULT_DOWNLOAD_URL

            SwingUtilities.invokeLater {
                val confirmDialog =
                    JOptionPane.showConfirmDialog(null, "发现新版本，是否下载？", "发现新版本", JOptionPane.YES_NO_OPTION)
                if (confirmDialog == JOptionPane.YES_OPTION) {
                    DesktopUtil.browse(downloadUrl)
                }
            }
        }
    }
}
