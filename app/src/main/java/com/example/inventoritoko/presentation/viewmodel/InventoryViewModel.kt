package com.example.inventoritoko.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoritoko.data.api.RetrofitClient
import com.example.inventoritoko.data.model.AddToCartRequest
import com.example.inventoritoko.data.model.CartItem
import com.example.inventoritoko.data.model.Product
import com.example.inventoritoko.data.model.UpdateCartQuantityRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(private val context: Context) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addToCartResult = MutableStateFlow<Boolean?>(null)
    val addToCartResult: StateFlow<Boolean?> = _addToCartResult.asStateFlow()

    private val _updateCartQuantityResult = MutableStateFlow<Boolean?>(null) // State baru
    val updateCartQuantityResult: StateFlow<Boolean?> = _updateCartQuantityResult.asStateFlow()

    private val _checkoutResult = MutableStateFlow<Boolean?>(null)
    val checkoutResult: StateFlow<Boolean?> = _checkoutResult.asStateFlow()

    private val _deleteCartItemResult = MutableStateFlow<Boolean?>(null)
    val deleteCartItemResult: StateFlow<Boolean?> = _deleteCartItemResult.asStateFlow()

    private val _clearCartResult = MutableStateFlow<Boolean?>(null)
    val clearCartResult: StateFlow<Boolean?> = _clearCartResult.asStateFlow()

    private val apiService = RetrofitClient.getApiService(context)

    init {
        fetchProducts()
        fetchCartItems()
    }

    fun fetchProducts() {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getProducts()
                if (response.isSuccessful) {
                    _products.value = response.body() ?: emptyList()
                } else {
                    _error.value = response.errorBody()?.string() ?: "Failed to fetch products"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error fetching products"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchCartItems() {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getCart()
                if (response.isSuccessful) {
                    _cartItems.value = response.body() ?: emptyList()
                } else {
                    _error.value = response.errorBody()?.string() ?: "Failed to fetch cart items"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error fetching cart items"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToCart(productId: Int, quantity: Int) {
        _addToCartResult.value = null // Reset state
        viewModelScope.launch {
            try {
                val request = AddToCartRequest(productId, quantity)
                val response = apiService.addToCart(request)
                if (response.isSuccessful) {
                    _addToCartResult.value = true
                    fetchCartItems() // Refresh cart after adding
                } else {
                    _addToCartResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to add to cart"
                }
            } catch (e: Exception) {
                _addToCartResult.value = false
                _error.value = e.message ?: "Network error adding to cart"
            }
        }
    }

    fun updateCartItemQuantity(productId: Int, newQuantity: Int) {
        _updateCartQuantityResult.value = null // Reset state
        viewModelScope.launch {
            try {
                val request = UpdateCartQuantityRequest(newQuantity)
                val response = apiService.updateCartItemQuantity(productId, request)
                if (response.isSuccessful) {
                    _updateCartQuantityResult.value = true
                    fetchCartItems() // Refresh cart after updating
                } else {
                    _updateCartQuantityResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to update cart quantity"
                }
            } catch (e: Exception) {
                _updateCartQuantityResult.value = false
                _error.value = e.message ?: "Network error updating cart quantity"
            }
        }
    }


    fun deleteCartItem(productId: Int) {
        _deleteCartItemResult.value = null // Reset state
        viewModelScope.launch {
            try {
                val response = apiService.deleteCartItem(productId)
                if (response.isSuccessful) {
                    _deleteCartItemResult.value = true
                    fetchCartItems() // Refresh cart after deleting
                } else {
                    _deleteCartItemResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to delete item from cart"
                }
            } catch (e: Exception) {
                _deleteCartItemResult.value = false
                _error.value = e.message ?: "Network error deleting item from cart"
            }
        }
    }

    fun clearCart() {
        _clearCartResult.value = null // Reset state
        viewModelScope.launch {
            try {
                val response = apiService.clearCart()
                if (response.isSuccessful) {
                    _clearCartResult.value = true
                    fetchCartItems() // Refresh cart after clearing
                } else {
                    _clearCartResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Failed to clear cart"
                }
            } catch (e: Exception) {
                _clearCartResult.value = false
                _error.value = e.message ?: "Network error clearing cart"
            }
        }
    }

    fun checkout() {
        _checkoutResult.value = null // Reset state
        viewModelScope.launch {
            try {
                val response = apiService.checkout()
                if (response.isSuccessful) {
                    _checkoutResult.value = true
                    _cartItems.value = emptyList() // Clear cart after successful checkout
                } else {
                    _checkoutResult.value = false
                    _error.value = response.errorBody()?.string() ?: "Checkout failed"
                }
            } catch (e: Exception) {
                _checkoutResult.value = false
                _error.value = e.message ?: "Network error during checkout"
            }
        }
    }

    fun clearAddToCartResult() {
        _addToCartResult.value = null
    }

    fun clearUpdateCartQuantityResult() { // Fungsi baru untuk membersihkan state
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
