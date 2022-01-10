package com.hiar.ar110.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.YuvImage
import com.hiar.ar110.data.FaceIcon
import com.hiar.mybaselib.recog.FaceRecognitionInfo
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.BitmapUtils
import java.io.ByteArrayOutputStream

/**
 * Author:wilson.chen
 * date：6/24/21
 * desc：
 */
const val TAG = "FaceDataParser"


fun genFaceIcons(faces: List<FaceRecognitionInfo>, image: YuvImage, width: Int, height: Int): Array<FaceIcon?> {
    val faceArray = arrayOfNulls<FaceIcon>(faces.size)
    for (index in faces.indices) {
        val baos = ByteArrayOutputStream()
        val calc = ByteArrayOutputStream()
        val faceId = faces[index].face_id.toString()
        val rect = updateRect(faces[index].bbox, width, height)
        image.compressToJpeg(rect, 90, baos)
        var minEdge: Float = (rect.right - rect.left).coerceAtMost(rect.bottom - rect.top).toFloat()
        AR110Log.i(TAG, "minEdge: $minEdge")
        minEdge = 64 / minEdge
        AR110Log.i(TAG, "minEdge: $minEdge")
        if (1 < minEdge) {
            val imageBytes = baos.toByteArray()
            val imageR = BitmapFactory.decodeByteArray(imageBytes, 0,
                    imageBytes.size)
            val scaleImg = BitmapUtils.getScaledBitmap(imageR, minEdge, minEdge)
            scaleImg.compress(Bitmap.CompressFormat.JPEG, 90, calc)
            val base64 = Base64Util.byteToBase64NoLine(calc.toByteArray())
            faceArray[index] = FaceIcon(faceId, base64)
        } else {
            val base64 = Base64Util.byteToBase64NoLine(baos.toByteArray())
            faceArray[index] = FaceIcon(faceId, base64)
        }
    }
    return faceArray
}

// 增大人脸框
private fun updateRect(rect: Rect, width: Int, height: Int): Rect {
    val roiWidth: Int = rect.right - rect.left
    val roiHeight: Int = rect.bottom - rect.top
    rect.left -= (0.3 * roiWidth).toInt()
    rect.right += (0.3 * roiWidth).toInt()
    rect.top -= (0.45 * roiHeight).toInt()
    rect.bottom += (0.25 * roiHeight).toInt()

    val roiFace = Rect()
    roiFace.left = if (rect.left > 0) rect.left else 0
    roiFace.top = if (rect.top > 0) rect.top else 0
    roiFace.right = if (rect.right < width) rect.right else width - 1
    roiFace.bottom = if (rect.bottom < height) rect.bottom else height - 1
    return roiFace
}