package com.hiar.ar110.data

/**
 * Author:wilson.chen
 * date：5/19/21
 * desc：
 */
data class MqttData<T>(
        var action: Int?= null,
        var payload: Payload<T>?= null,
)

data class Payload<T>(
        var id: Int? = null,
        var reponseField: T? = null
)

data class Coordinate(
        var longitude: Double,
        var latitude: Double
)

