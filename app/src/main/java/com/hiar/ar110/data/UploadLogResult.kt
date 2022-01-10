package com.hiar.ar110.data

import com.hiar.ar110.data.cop.PatrolRecord
import com.hiscene.gis.Position

/**
 * @author xuchengtang
 * @date 28/05/2021
 * Email: xucheng.tang@hiscene.com
 */
class UploadLogResult<T>(var name:  String? = null, var position:Int=0) : HttpResult<T>()