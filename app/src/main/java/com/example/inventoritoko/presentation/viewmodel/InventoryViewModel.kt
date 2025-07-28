// InventoryViewModel.kt
package com.example.inventoritoko.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoritoko.data.api.RetrofitClient
import com.example.inventoritoko.data.model.* // Import semua model data
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.StringReader

class InventoryViewModel(private val context: Context) : ViewModel() {

    private val TAG = "InventoryViewModel"

    // StateFlows untuk data dan status UI
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addToCartResult = MutableStateFlow<Boolean?>(null)
    val addToCartResult: StateFlow<Boolean?> = _addToCartResult.asStateFlow()

    private val _updateCartQuantityResult = MutableStateFlow<Boolean?>(null)
    val updateCartQuantityResult: StateFlow<Boolean?> = _updateCartQuantityResult.asStateFlow()

    private val _checkoutResult = MutableStateFlow<Boolean?>(null)
    val checkoutResult: StateFlow<Boolean?> = _checkoutResult.asStateFlow()

    private val _deleteCartItemResult = MutableStateFlow<Boolean?>(null)
    val deleteCartItemResult: StateFlow<Boolean?> = _deleteCartItemResult.asStateFlow()

    private val _clearCartResult = MutableStateFlow<Boolean?>(null)
    val clearCartResult: StateFlow<Boolean?> = _clearCartResult.asStateFlow()

    private val _directCheckoutProduct = MutableStateFlow<Product?>(null)
    val directCheckoutProduct: StateFlow<Product?> = _directCheckoutProduct.asStateFlow()

    private val _directCheckoutQuantity = MutableStateFlow(1)
    val directCheckoutQuantity: StateFlow<Int> = _directCheckoutQuantity.asStateFlow()

    // StateFlow untuk riwayat pembelian (sekarang List<PurchaseHistoryItem>)
    private val _purchaseHistory = MutableStateFlow<List<PurchaseHistoryItem>>(emptyList())
    val purchaseHistory: StateFlow<List<PurchaseHistoryItem>> = _purchaseHistory.asStateFlow()

    // Inisialisasi ApiService
    private val apiService = RetrofitClient.getApiService(context)

    // Blok inisialisasi ViewModel
    init {
        Log.d(TAG, "InventoryViewModel initialized. Fetching products and cart items.")
        fetchProducts() // Dipanggil di sini
        fetchCartItems() // Dipanggil di sini
    }

    // --- Fungsi-fungsi untuk operasi Inventory ---

    fun fetchProducts() {
        Log.d(TAG, "fetchProducts() called.")
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getProducts()
                if (response.isSuccessful) {
                    _products.value = response.body() ?: emptyList()
                    Log.d(TAG, "Products fetched successfully: ${_products.value.size} items.")
                } else {
                    _error.value = response.errorBody()?.string() ?: "Failed to fetch products"
                    Log.e(TAG, "Failed to fetch products: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error fetching products"
                Log.e(TAG, "Network error fetching products: ${_error.value}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchCartItems() {
        Log.d(TAG, "fetchCartItems() called.")
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getCart()
                if (response.isSuccessful) {
                    _cartItems.value = response.body() ?: emptyList()
                    Log.d(TAG, "Cart items fetched successfully: ${_cartItems.value.size} items.")
                } else {
                    _error.value = response.errorBody()?.string() ?: "Failed to fetch cart items"
                    Log.e(TAG, "Failed to fetch cart items: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error fetching cart items"
                Log.e(TAG, "Network error fetching cart items: ${_error.value}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchProductById(productId: Int) {
        Log.d(TAG, "fetchProductById($productId) called.")
        _loading.value = true
        _error.value = null
        _selectedProduct.value = null
        _directCheckoutProduct.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getProductById(productId)
                if (response.isSuccessful) {
                    _selectedProduct.value = response.body()
                    _directCheckoutProduct.value = response.body()
                    Log.d(TAG, "Product details fetched successfully for ID: $productId")
                } else {
                    _error.value = response.errorBody()?.string() ?: "Failed to fetch product details"
                    Log.e(TAG, "Failed to fetch product details for ID $productId: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error fetching product details"
                Log.e(TAG, "Network error fetching product details for ID $productId: ${_error.value}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToCart(productId: Int, quantity: Int) {
        Log.d(TAG, "addToCart($productId, $quantity) called.")
        _addToCartResult.value = null
        viewModelScope.launch {
            try {
                val request = AddToCartRequest(productId, quantity)
                val response = apiService.addToCart(request)
                if (response.isSuccessful) {
                    _addToCartResult.value = true
                    fetchCartItems()
                    Log.d(TAG, "Product $productId added to cart successfully.")
                } else {
                    _addToCartResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to add to cart"
                    Log.e(TAG, "Failed to add product $productId to cart: ${_error.value}")
                }
            } catch (e: Exception) {
                _addToCartResult.value = false
                _error.value = e.message ?: "Network error adding to cart"
                Log.e(TAG, "Network error adding product $productId to cart: ${_error.value}", e)
            }
        }
    }

    fun updateCartItemQuantity(productId: Int, newQuantity: Int) {
        Log.d(TAG, "updateCartItemQuantity($productId, $newQuantity) called.")
        _updateCartQuantityResult.value = null
        viewModelScope.launch {
            try {
                val request = UpdateCartQuantityRequest(newQuantity)
                val response = apiService.updateCartItemQuantity(productId, request)
                if (response.isSuccessful) {
                    _updateCartQuantityResult.value = true
                    fetchCartItems()
                    Log.d(TAG, "Cart quantity for product $productId updated to $newQuantity.")
                } else {
                    _updateCartQuantityResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to update cart quantity"
                    Log.e(TAG, "Failed to update cart quantity for product $productId: ${_error.value}")
                }
            } catch (e: Exception) {
                _updateCartQuantityResult.value = false
                _error.value = e.message ?: "Network error updating cart quantity"
                Log.e(TAG, "Network error updating cart quantity for product $productId: ${_error.value}", e)
            }
        }
    }

    fun updateDirectCheckoutQuantity(newQuantity: Int) {
        Log.d(TAG, "updateDirectCheckoutQuantity($newQuantity) called.")
        _directCheckoutQuantity.value = newQuantity.coerceAtLeast(1)
    }

    fun deleteCartItem(productId: Int) {
        Log.d(TAG, "deleteCartItem($productId) called.")
        _deleteCartItemResult.value = null
        viewModelScope.launch {
            try {
                val response = apiService.deleteCartItem(productId)
                if (response.isSuccessful) {
                    _deleteCartItemResult.value = true
                    fetchCartItems()
                    Log.d(TAG, "Product $productId deleted from cart successfully.")
                } else {
                    _deleteCartItemResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to delete item from cart"
                    Log.e(TAG, "Failed to delete product $productId from cart: ${_error.value}")
                }
            } catch (e: Exception) {
                _deleteCartItemResult.value = false
                _error.value = e.message ?: "Network error deleting item from cart"
                Log.e(TAG, "Network error deleting product $productId from cart: ${_error.value}", e)
            }
        }
    }

    fun clearCart() {
        Log.d(TAG, "clearCart() called.")
        _clearCartResult.value = null
        viewModelScope.launch {
            try {
                val response = apiService.clearCart()
                if (response.isSuccessful) {
                    _clearCartResult.value = true
                    _cartItems.value = emptyList()
                    Log.d(TAG, "Cart cleared successfully.")
                } else {
                    _clearCartResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to clear cart"
                    Log.e(TAG, "Failed to clear cart: ${_error.value}")
                }
            } catch (e: Exception) {
                _clearCartResult.value = false
                _error.value = e.message ?: "Network error clearing cart"
                Log.e(TAG, "Network error clearing cart: ${_error.value}", e)
            }
        }
    }

    fun checkout() {
        Log.d(TAG, "checkout() (cart) called.")
        _checkoutResult.value = null
        viewModelScope.launch {
            try {
                val response = apiService.checkout()
                if (response.isSuccessful) {
                    _checkoutResult.value = true
                    _cartItems.value = emptyList()
                    Log.d(TAG, "Cart checkout successful.")
                } else {
                    _checkoutResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Checkout failed"
                    Log.e(TAG, "Cart checkout failed: ${_error.value}")
                }
            } catch (e: Exception) {
                _checkoutResult.value = false
                _error.value = e.message ?: "Network error during checkout"
                Log.e(TAG, "Network error during cart checkout: ${_error.value}", e)
            }
        }
    }

    fun directCheckout(productId: Int, quantity: Int) {
        Log.d(TAG, "directCheckout($productId, $quantity) called.")
        _checkoutResult.value = null
        viewModelScope.launch {
            try {
                val request = DirectCheckoutRequest(productId, quantity)
                val response = apiService.directCheckout(request)
                if (response.isSuccessful) {
                    _checkoutResult.value = true
                    Log.d(TAG, "Direct checkout for product $productId successful.")
                } else {
                    _checkoutResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Direct checkout failed"
                    Log.e(TAG, "Direct checkout for product $productId failed: ${_error.value}")
                }
            } catch (e: Exception) {
                _checkoutResult.value = false
                _error.value = e.message ?: "Network error during direct checkout"
                Log.e(TAG, "Network error during direct checkout for product $productId: ${_error.value}", e)
            }
        }
    }

    // Fungsi untuk mengambil riwayat pembelian dengan parsing manual
    fun fetchPurchaseHistory() {
        Log.d(TAG, "fetchPurchaseHistory() called.")
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getPurchaseHistory()
                if (response.isSuccessful) {
                    val jsonString = response.body()?.string()
                    Log.d(TAG, "Raw JSON response body for history: $jsonString")
                    if (jsonString != null) {
                        val parsedHistory = parseFlatPurchaseHistoryJson(jsonString)
                        _purchaseHistory.value = parsedHistory
                        Log.d(TAG, "Purchase history parsed successfully: ${parsedHistory.size} items.")
                    } else {
                        _error.value = "Failed to get response body for purchase history."
                        Log.e(TAG, _error.value!!)
                    }
                } else {
                    _error.value = response.errorBody()?.string() ?: "Failed to fetch purchase history"
                    Log.e(TAG, "Failed to fetch purchase history: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error fetching purchase history"
                Log.e(TAG, "Network error fetching purchase history: ${_error.value}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // Fungsi parsing JSON manual untuk daftar datar PurchaseHistoryItem
    private fun parseFlatPurchaseHistoryJson(jsonString: String): List<PurchaseHistoryItem> {
        val historyItems = mutableListOf<PurchaseHistoryItem>()
        val reader = JsonReader(StringReader(jsonString))

        reader.beginArray()
        while (reader.hasNext()) {
            reader.beginObject()
            var transactionId: Int = 0
            var transactionTotalPrice: String? = null
            var transactionCreatedAt: String? = null
            var itemId: Int = 0
            var productId: Int = 0
            var quantity: Int = 0
            var itemPrice: String? = null
            var productName: String = ""
            var productImage: String? = null

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "transactionId" -> transactionId = reader.nextInt()
                    "transactionTotalPrice" -> {
                        transactionTotalPrice = if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                            null
                        } else {
                            reader.nextString()
                        }
                        Log.d(TAG, "Manual Parse - transactionTotalPrice read as: '$transactionTotalPrice'")
                    }
                    "transactionCreatedAt" -> {
                        transactionCreatedAt = if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                            null
                        } else {
                            reader.nextString()
                        }
                    }
                    "itemId" -> itemId = reader.nextInt()
                    "productId" -> productId = reader.nextInt()
                    "quantity" -> quantity = reader.nextInt()
                    "itemPrice" -> {
                        itemPrice = if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                            null
                        } else {
                            reader.nextString()
                        }
                        Log.d(TAG, "Manual Parse - itemPrice read as: '$itemPrice'")
                    }
                    "productName" -> productName = reader.nextString()
                    "productImage" -> {
                        productImage = if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                            null
                        } else {
                            reader.nextString()
                        }
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            historyItems.add(
                PurchaseHistoryItem(
                    transactionId,
                    transactionTotalPrice,
                    transactionCreatedAt,
                    itemId,
                    productId,
                    quantity,
                    itemPrice,
                    productName,
                    productImage
                )
            )
        }
        reader.endArray()
        reader.close()
        return historyItems
    }

    // Fungsi-fungsi untuk membersihkan hasil
    fun clearAddToCartResult() {
        _addToCartResult.value = null
    }

    fun clearUpdateCartQuantityResult() {
        _updateCartQuantityResult.value = null
    }

    fun clearCheckoutResult() {
        _checkoutResult.value = null
    }

    fun clearDeleteCartItemResult() {
        _deleteCartItemResult.value = null
    }

    fun clearClearCartResult() {
        _clearCartResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}