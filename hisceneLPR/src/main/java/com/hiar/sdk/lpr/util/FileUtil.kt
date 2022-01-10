package com.hiar.sdk.lpr.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 *
 * @author wilson
 * @date 29/10/2021
 * Email: haiqin.chen@hiscene.com
 */
object FileUtil {
    fun copyAssetGetFilePath(context: Context, fileName: String?): String? {
        return try {
            val cacheDir = context.cacheDir
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val outFile = File(cacheDir, fileName)
            if (!outFile.exists()) {
                val res = outFile.createNewFile()
                if (!res) {
                    return null
                }
            } else if (outFile.length() > 10L) {
                return outFile.path
            }
            val `is` = context.assets.open(fileName!!)
            val fos = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var byteCount: Int
            while (`is`.read(buffer).also { byteCount = it } != -1) {
                fos.write(buffer, 0, byteCount)
            }
            fos.flush()
            `is`.close()
            fos.close()
            outFile.path
        } catch (var8: IOException) {
            var8.printStackTrace()
            null
        }
    }
}