package com.peekly.parent.network

import com.peekly.parent.data.ChildDto
import com.peekly.parent.data.CreateChildRequest
import com.peekly.parent.data.DigestDto
import com.peekly.parent.data.FcmTokenRequest
import com.peekly.parent.data.PairingCodeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PeeklyParentApi {

    @GET("children")
    suspend fun getChildren(@Query("parentSub") parentSub: String): Response<List<ChildDto>>

    @POST("children")
    suspend fun createChild(@Body request: CreateChildRequest): Response<ChildDto>

    @POST("pairing/generate")
    suspend fun generatePairingCode(@Query("childId") childId: Long): Response<PairingCodeDto>

    @GET("digest/{childId}")
    suspend fun getDigest(@Path("childId") childId: Long): Response<DigestDto>

    @GET("digest/{childId}/history")
    suspend fun getDigestHistory(@Path("childId") childId: Long): Response<List<DigestDto>>

    @POST("parent/fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): Response<Unit>
}
