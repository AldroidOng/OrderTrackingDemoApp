package com.biz.aceras.ordertracking.serializer_class

import com.google.gson.annotations.SerializedName

/**
 * Created by eesern_ong on 10/4/2019.
 */
class OrderDetailsInfo {

    @SerializedName("Header")
    var salesOrderHeader: OrderHeaderInfo = OrderHeaderInfo()

    @SerializedName("Body")
    var salesOrderItem: List<OrderItemInfo> = emptyList()
}