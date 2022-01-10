package com.hiar.ar110.data

/**
 * Author:wilson.chen
 * date：5/19/21
 * desc：
 */
data class UserIdInfo(
        var id: Int?= null,
        var tagId: String?= null,

) {
        override fun toString(): String {
                return "UserIdInfo(id=$id, tagId=$tagId)"
        }
}
