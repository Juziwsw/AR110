package com.hiar.ar110.config

interface LoginConstants {
    companion object {
        const val LoginKey = "unicorn-portal-zt-ar110"
        const val LoginAesKey = "1234567887654321"
        const val LoginTime = "login_time"
        const val LOGIN_NAME = "login_name"
        const val TimeDifferenceComparisonValue = 60*60 //小时单位
        const val MAX_TimeDifference = 6
    }
}

