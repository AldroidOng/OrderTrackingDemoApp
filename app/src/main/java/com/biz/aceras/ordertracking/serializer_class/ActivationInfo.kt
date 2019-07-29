package com.biz.aceras.ordertracking.serializer_class

import com.google.gson.annotations.SerializedName

/**
 * Created by eesern_ong on 10/4/2019.
 */
class ActivationInfo {
    @SerializedName("RegVerifyRegCode")
    var cacheActivationCode: String = ""

    @SerializedName("RegVerifyAC")
    var smsActivationCode: String = ""

    @SerializedName("Imei")
    var imeiNo: String = ""
}