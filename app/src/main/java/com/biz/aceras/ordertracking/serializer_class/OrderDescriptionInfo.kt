package com.biz.aceras.ordertracking.serializer_class

import com.google.gson.annotations.SerializedName

/**
 * Created by eesern_ong on 10/4/2019.
 */
class OrderDescriptionInfo {
    @SerializedName("OrderNumber")
    var orderNumber: String = ""

    @SerializedName("ItemNumber")
    var itemNumber: String = ""

    @SerializedName("ProductDesc")
    var productDesc: String = ""

    @SerializedName("ProductQty")
    var productQty: String = ""

    @SerializedName("FinalDestination")
    var finalDestination: String = ""

    @SerializedName("TargetConfirmed")
    var targetConfirmed: String = ""

    @SerializedName("TargetProduced")
    var targetProduced: String = ""

    @SerializedName("TargetShipped")
    var targetShipped: String = ""

    @SerializedName("TargetDelivered")
    var targetDelivered: String = ""

    @SerializedName("OrderTaken")
    var actualTaken: String = ""

    @SerializedName("ActualConfirmed")
    var actualConfirmed: String = ""

    @SerializedName("ActualProduced")
    var actualProduced: String = ""

    @SerializedName("ActualShipped")
    var actualShipped: String = ""

    @SerializedName("ActualDelivered")
    var actualDelivered: String = ""

    var intItemNumber: Int = 0

    var status: String = ""

    var statusDate: String = ""
}