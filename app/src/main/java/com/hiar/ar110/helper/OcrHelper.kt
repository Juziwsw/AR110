package com.hiar.ar110.helper

/**
 * @author tangxucheng
 * @date 2021/6/8
 * Email: xucheng.tang@hiscene.com
 * @Deprecated
 */
object OcrHelper {
    val TAG = this.javaClass.simpleName


    abstract class OcrListener {
        abstract fun onSingleResult(fieldvalue: Array<String?>?, datas: ByteArray?)
    }
}