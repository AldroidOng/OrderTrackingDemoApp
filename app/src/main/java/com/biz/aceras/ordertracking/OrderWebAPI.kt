package com.biz.aceras.ordertracking

import com.biz.aceras.ordertracking.serializer_class.*
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by eesern_ong on 10/4/2019.
 */
interface OrderWebAPI {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @GET("april/api/binary")
    fun getImageBinary(): Call<String>

    @POST("april/api/register")
    fun register(@Body registerInfo: RegisterInfo): Call<String>

    @POST("april/api/RegVerify")
    fun activate(@Body activateInfo: ActivationInfo): Call<String>

    @POST("april/api/ResendAC")
    fun resendAC(@Body resendACInfo: ResendACInfo): Call<String>

    @POST("april/api/valid")
    fun checkSession(@Body checkSessionInfo: CheckSessionInfo): Call<Void>

    @POST("april/api/login")
    fun login(@Body loginInfo: LoginInfo): Call<String>

    @POST("april/api/OTPVerify")
    fun verifyOTP(@Body verifyInfo: VerifyInfo): Call<String>

    @POST("access/api/banner")
    fun getBanner(): Call<List<BannerInfo>>

    @POST ("access/api/dropdown")
    fun getProductType(@Body getProductTypeInfo:GetProductTypeInfo): Call<List<ProductTypeInfo>>

    @POST ("access/api/OrderID")
    fun getOrderDetails(@Body trackInfo:TrackInfo): Call<OrderDetailsInfo>

    @POST ("access/api/OrderID")
    fun getOrderList(@Body trackInfo:TrackInfo): Call<List<OrderHeaderInfo>>
    // TO BE CHANGED
//    @POST("access/api/OrderID")
//    fun trackOrder(@Body verifyInfo: VerifyInfo): Call<String>


//    @POST("account/register")
//    fun getUserID(@Body registerInfo: UserInfo): Call<String>
//
//    @POST("account/resendOTP")
//    fun getNewOTP(@Body userId: UserIdInfo): Call<Void>
//
//    @POST("account/verifyOTP")
//    fun verifyOTP(@Body verify: VerifyInfo): Call<String>
//
//    @GET("tracking/{no}")
//    fun trackOrder(@Path("no") orderNumber: String, @Header("SessionID") sessionID: String): Call<OrderHeaderInfo>
//
//    @GET("banner")
//    fun banner(): Call<List<BannerInfo>>
}