package com.hiar.sdk.lpr.core

import java.util.*

/**
 *
 * @author wilson
 * @date 02/11/2021
 * Email: haiqin.chen@hiscene.com
 */
enum class LPType(val code: Int, val desc: String, val size: Int) {
    LP_OTHRE_TYPE(0, "未知", 7),
    LP_BLUE_NORMAL(1, "蓝", 7),
    LP_YELLOW_NORMAL(2, "黄", 7),
    LP_YELLOW_DOUBLE_TRUCK(3, "黄", 7),
    LP_YELLOW_DOUBLE_MOTO(4, "黄", 7),
    LP_WHITE_JING(5, "白", 7),
    LP_WHITE_WUJING(6, "白", 7),
    LP_WHITE_JUN(7, "白", 7),
    LP_BLACK_NORMAL(8, "黑", 7),
    LP_BLACK_GANG_AO(9, "黑", 7),
    LP_BLACK_LING(10, "黑", 7),
    LP_BLACK_SHI(11, "黑", 7),
    LP_GREEN_ENERGY(12, "绿", 8),
    LP_GREEN_BUS(13, "绿", 8),
    LP_YELLOW_STUDENT(14, "黄", 7),
    LP_UNKNOW_TYPE(30, "未知", 7);

    companion object {
        fun getTypeByCode(code: Int): LPType {
            return Arrays.stream(values())
                .filter { t: LPType -> t.code == code }
                .findFirst()
                .orElse(LP_UNKNOW_TYPE) as LPType
        }
    }
}
