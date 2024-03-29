package com.biz.aceras.ordertracking.serializer_class

import com.google.gson.annotations.SerializedName

/**
 * Created by eesern_ong on 10/4/2019.
 */
class GetProductTypeInfo {
    @SerializedName("SalesRegCode")
    var registrationID: String = ""

    @SerializedName("Imei")
    var imeiNo: String = ""

    @SerializedName("Token")
    var sessionTokenID: String = ""
}