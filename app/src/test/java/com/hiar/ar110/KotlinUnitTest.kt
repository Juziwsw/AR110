package com.hiar.ar110

import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.extension.fromJson
import com.hiar.ar110.extension.fromJsonList
import com.hiar.ar110.extension.toJson
import com.hiscene.imui.util.TimeUtils
import org.junit.Test
import java.io.File

/**
 * Author:wilson.chen
 * date：5/18/21
 * desc：
 */
class KotlinUnitTest {

    @Test
    fun testJsonToObject() {
        val json = "{\"retCode\":0,\"data\":{\"token\":\"cd1d8dd32be3263c32c2f99afab9dc54\"}}"
        val result = json.fromJson<TestDataBean?>()
        result?.let {
            println("token=${it.data.token}")
            assert(it.data.token.equals("cd1d8dd32be3263c32c2f99afab9dc54"))
        }
    }

    @Test
    fun testJsonToArray() {
        val json = "[{\"retCode\":0,\"data\":{\"token\":\"cd1d8dd32be3263c32c2f99afab9dc54\"}},{\"retCode\":0,\"data\":{\"token\":\"cd1d8dd32be3263c32c2f99afab9dc54\"}}]"
        val result = json.fromJsonList<TestDataBean>()
        result?.mapNotNull {
            println("token=${it.data.token}")
            assert(it.data.token.equals("cd1d8dd32be3263c32c2f99afab9dc54"))
        }
    }

    @Test
    fun testObjectToJson() {
        val json = "{\"retCode\":0,\"data\":{\"token\":\"cd1d8dd32be3263c32c2f99afab9dc54\"}}"
        val dataBean = TestDataBean(0, BoddyBean("cd1d8dd32be3263c32c2f99afab9dc54"))
        val result = dataBean.toJson()
        result?.let {
            println("result=${it}")
            assert(it.equals(json))
        }
    }

    @Test
    fun testArrayToJson() {
        val json = "[{\"retCode\":0,\"data\":{\"token\":\"cd1d8dd32be3263c32c2f99afab9dc54\"}},{\"retCode\":1,\"data\":{\"token\":\"cd1d8dd32be3263c32c2f99afab9dc54\"}}]"
        val list = mutableListOf<TestDataBean>()
        list.add(TestDataBean(0, BoddyBean("cd1d8dd32be3263c32c2f99afab9dc54")))
        list.add(TestDataBean(1, BoddyBean("cd1d8dd32be3263c32c2f99afab9dc54")))
        val result = list.toJson()
        result?.let {
            println("result=${it}")
            assert(it.equals(json))
        }
    }

    @Test
    fun testGetLaterDayTime() {
        val days = 1
        val result = TimeUtils.getLaterDayTime(days)
        println("result =$result")
        assert(result>0)
    }

    @Test
    fun testSort(){
        val list = mutableListOf<BoddyBean>()
        list.add(BoddyBean("2021-04-19-11.mp4"))
        list.add(BoddyBean("2021-04-29-01.mp4"))
        list.add(BoddyBean("2021-04-09-21.mp4"))
        list.add(BoddyBean("2021-04-11-21.mp4"))
        list.add(BoddyBean("2021-04-11-19.mp4"))
        list.sortByDescending {
            it.token
        }
        println(list.toJson())

    }
    @Test
    fun testHashEncrypt(){
        val data ="123456"
        val result3 =EncryptUtils.encryptMD5ToString(data.toByteArray())
        println("result=$result3")
        println("is match=${result3.toLowerCase()=="e10adc3949ba59abbe56e057f20f883e"}")
    }

    data class TestDataBean(
            val retCode: Int,
            val data: BoddyBean,

            )

    data class BoddyBean(
            val token: String
    )
}