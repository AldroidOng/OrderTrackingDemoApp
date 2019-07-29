package com.biz.aceras.ordertracking.serializer_class

import com.google.gson.annotations.SerializedName

/**
 * Created by eesern_ong on 23/4/2019.
 */
class BannerInfo {

    @SerializedName("BannerImage")
    var imageUrl: String = ""

    @SerializedName("BannerLink")
    var redirectUrl: String = ""
}