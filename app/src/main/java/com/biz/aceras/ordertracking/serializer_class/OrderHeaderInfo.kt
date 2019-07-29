package com.biz.aceras.ordertracking.serializer_class

import com.google.gson.annotations.SerializedName

/**
 * Created by eesern_ong on 10/4/2019.
 */
class OrderHeaderInfo {
    @SerializedName("OrderNumber")
    var orderNumber: String = ""

    @SerializedName("CustomerName")
    var customerName: String = ""

    @SerializedName("ConsigneeName")
    var consigneeName: String = ""

    @SerializedName("PONumber")
    var pONumber: String = ""

    @SerializedName("ProductionOrder")
    var productionOrder: String = ""

    @SerializedName("Incoterms")
    var incoterms: String = ""

    @SerializedName("SalesOfficeText")
    var salesOfficeText: String = ""

    @SerializedName("Quantity")
    var totalQuantity: String = ""

    @SerializedName("ETA")
    var estimatedDateOfArrival: String = ""

    @SerializedName("OrderTaken")
    var earliestOrderTaken: String = ""

}