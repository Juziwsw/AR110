package com.hiar.ar110.event

class CommEvent @JvmOverloads constructor(val tag: String, var data: Any? = null,var data2: Any? = null,var data3: Any? = null)

fun CommEvent.sendEvent(){
    EventLiveBus.commtEvent.postValue(this)
}