package com.hiar.sdk.lpr.zip

import android.util.Log
import com.hiar.mybaselib.utils.StringUtils
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 *
 * @author wilson
 * @date 29/10/2021
 * Email: haiqin.chen@hiscene.com
 */
object ZipUtils {
    private const val BUFFER_LEN = 8192

    @Throws(IOException::class)
    fun unzipFile(
        zipFilePath: String?,
        destDirPath: String?
    ): List<File?>? {
        return unzipFileByKeyword(zipFilePath, destDirPath, null)
    }
    @Throws(IOException::class)
    fun unzipFileByKeyword(
        zipFilePath: String?,
        destDirPath: String?,
        keyword: String?
    ): List<File?>? {
        return unzipFileByKeyword(
            getFileByPath(zipFilePath),
            getFileByPath(destDirPath),
            keyword
        )
    }

    /**
     * Unzip the file by keyword.
     *
     * @param zipFile The ZIP file.
     * @param destDir The destination directory.
     * @param keyword The keyboard.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    @Throws(IOException::class)
    fun unzipFileByKeyword(
        zipFile: File?,
        destDir: File?,
        keyword: String?
    ): List<File>? {
        if (zipFile == null || destDir == null) return null
        val files = mutableListOf<File>()
        val zip = ZipFile(zipFile)
        val entries: Enumeration<*> = zip.entries()
        try {
            if (StringUtils.isEmpty(keyword)) {
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement() as ZipEntry
                    val entryName = entry.name.replace("\\", "/")
                    if (entryName.contains("../")) {
                        Log.e("ZipUtils", "entryName: $entryName is dangerous!")
                        continue
                    }
                    if (!unzipChildFile(
                            destDir,
                            files,
                            zip,
                            entry,
                            entryName
                        )
                    ) return files
                }
            } else {
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement() as ZipEntry
                    val entryName = entry.name.replace("\\", "/")
                    if (entryName.contains("../")) {
                        Log.e("ZipUtils", "entryName: $entryName is dangerous!")
                        continue
                    }
                    if (entryName.contains(keyword!!)) {
                        if (!unzipChildFile(
                                destDir,
                                files,
                                zip,
                                entry,
                                entryName
                            )
                        ) return files
                    }
                }
            }
        } finally {
            zip.close()
        }
        return files
    }

    @Throws(IOException::class)
    private fun unzipChildFile(
        destDir: File,
        files: MutableList<File>,
        zip: ZipFile,
        entry: ZipEntry,
        name: String
    ): Boolean {
        val file = File(destDir, name)
        files.add(file)
        if (entry.isDirectory) {
            return createOrExistsDir(file)
        } else {
            if (!createOrExistsFile(file)) return false
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                `in` = BufferedInputStream(zip.getInputStream(entry))
                out = BufferedOutputStream(FileOutputStream(file))
                val buffer = ByteArray(ZipUtils.BUFFER_LEN)
                var len: Int
                while (`in`.read(buffer).also { len = it } != -1) {
                    out.write(buffer, 0, len)
                }
            } finally {
                `in`?.close()
                out?.close()
            }
        }
        return true
    }

    private fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        if (file.exists()) return file.isFile
        return if (!createOrExistsDir(file.parentFile)) false else try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    private fun getFileByPath(filePath: String?): File? {
        return if (StringUtils.isEmpty(filePath)) null else File(filePath)
    }
}