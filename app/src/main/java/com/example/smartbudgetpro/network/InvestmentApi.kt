package com.example.smartbudgetpro.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface InvestmentApi {

    @GET("coins/markets")
    suspend fun getTopCrypto(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true
    ): List<CryptoResponse>

    data class CryptoResponse(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("symbol") val symbol: String,
        @SerializedName("current_price") val currentPrice: Float,
        @SerializedName("price_change_percentage_24h") val change24h: Float,
        @SerializedName("sparkline_in_7d") val sparkline: Sparkline
    )

    data class Sparkline(
        @SerializedName("price") val price: List<Float>
    )
}