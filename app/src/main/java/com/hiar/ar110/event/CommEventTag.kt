package com.hiar.ar110.event

object CommEventTag {
    /**
     * data:CopTaskRecord
     * data2:String
     */
    const val COP_VIDEO_TASK_UPLOAD_COMPLETED = "cop_video_task_upload_completed"

    /**
     * data:PatrolRecord
     * data2:String
     */
    const val PATROL_VIDEO_TASK_UPLOAD_COMPLETED = "patrol_video_task_upload_completed"

    /**
     * data:boolean
     */
    const val GLASS_CONNECT_STATE_CHANGE = "glass_connect_state_change"

    /**
     * 模块化更新
     */
    const val MODULE_CONFIG_UPDATE = "module_config_update"
    /**
     * data:String
     */
    const val COPTASK_ADD = "coptask_add"
    /**
     * data:LocationData
     */
    const val LOCATION_UPDATE = "location_update"
    /**
     * 数据更新
     */
    const val DATA_UPDATE = "data_update"
    /**
     * 用戶数据更新
     */
    const val USER_INFO_UPDATE = "user_info_update"
}