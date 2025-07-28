package com.example.inventoritoko.data.api

import com.example.inventoritoko.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth Endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/forgotPassword")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    @POST("auth/resetPassword")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthResponse>

    // Inventory Endpoints
    @GET("inventory/products")
    suspend fun getProducts(): Response<List<Product>>

    @POST("inventory/cart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<AuthResponse> // Assuming a generic success response

    @PUT("inventory/cart/{productId}")
    suspend fun updateCartItemQuantity(
        @Path("productId") productId: Int,
        @Body request: UpdateCartQuantityRequest
    ): Response<AuthResponse>

    @GET("inventory/cart")
    suspend fun getCart(): Response<List<CartItem>>

    @DELETE("inventory/cart/{productId}")
    suspend fun deleteCartItem(@Path("productId") productId: Int): Response<AuthResponse>

    @DELETE("inventory/cart")
    suspend fun clearCart(): Response<AuthResponse>

    @POST("inventory/checkout")
    suspend fun checkout(): Response<CheckoutResponse>
}