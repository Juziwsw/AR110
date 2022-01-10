package com.hiar.ar110.data

import com.hiar.ar110.data.people.PopulationRecord

/**
 * Author:wilson.chen
 * date：5/26/21
 * desc：
 */
data class PopulationResultData(
        val total: Int = 0,
        val from: Int = 0,
        val limit: Int = 0,
        val verificationList: MutableList<PopulationRecord>?
)