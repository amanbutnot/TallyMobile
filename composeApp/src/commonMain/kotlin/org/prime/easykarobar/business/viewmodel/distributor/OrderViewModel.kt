package org.prime.easykarobar.business.viewmodel.distributor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.repository.OrderRepository
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.CancelOrderRequest
import org.prime.easykarobar.data.model.CancelOrderResponse
import org.prime.easykarobar.data.model.CreateOrderRequest
import org.prime.easykarobar.data.model.CreateOrderResponse
import org.prime.easykarobar.data.model.Order
import org.prime.easykarobar.data.model.Product
import org.prime.easykarobar.data.model.ProductCategoryList
import org.prime.easykarobar.data.model.UpdateOrderStatusRequest
import org.prime.easykarobar.data.utils.SharedPrefs
import org.tally.GetProductsForDis

class OrderViewModel : ViewModel() {

    private val _orderState = mutableStateOf(OrderState())
    val orderState: State<OrderState> = _orderState

    private val _createOrderState = mutableStateOf(CreateOrderState())
    val createOrderState: State<CreateOrderState> = _createOrderState

    private val _cancelOrderState = mutableStateOf(CancelOrderState())
    val cancelOrderState: State<CancelOrderState> = _cancelOrderState

    private val _listOrderState = mutableStateOf(ListOrderState())
    val listOrderState: State<ListOrderState> = _listOrderState

    private val _updateStatusState = mutableStateOf(UpdateStatusState())
    val updateStatusState: State<UpdateStatusState> = _updateStatusState


    fun createOrder(createOrderRequest: CreateOrderRequest) {
        viewModelScope.launch {
            _createOrderState.value = _createOrderState.value.copy(isLoading = true)
            val res = OrderRepository.createNewOrder(createOrderRequest)
            if (res?.statuscode == 200) {
                _createOrderState.value = CreateOrderState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
            } else {
                _createOrderState.value = CreateOrderState(
                    success = false,
                    isLoading = false,
                    message = res?.message ?: "Unexpected Error"
                )
            }
        }
    }

    fun cancelOrder(cancelOrderRequest: CancelOrderRequest) {
        viewModelScope.launch {
            _cancelOrderState.value = _cancelOrderState.value.copy(isLoading = true)
            val res = OrderRepository.cancelOrder(cancelOrderRequest)
            if (res?.statuscode == 200) {
                _cancelOrderState.value = CancelOrderState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
            } else {
                _cancelOrderState.value = CancelOrderState(
                    success = false,
                    isLoading = false,
                    message = res?.message ?: "Unexpected Error"
                )
            }
        }
    }

    fun updateOrderStatus(updateOrderStatusRequest: UpdateOrderStatusRequest) {
        viewModelScope.launch {
            _updateStatusState.value = _updateStatusState.value.copy(isLoading = true)
            val res = OrderRepository.updateOrderStatus(updateOrderStatusRequest)
            if (res?.statuscode == 200) {
                _updateStatusState.value = UpdateStatusState(
                    success = true,
                    isLoading = false,
                    message = res.message
                )
            } else {
                _updateStatusState.value = UpdateStatusState(
                    success = false,
                    isLoading = false,
                    message = res?.message ?: "Unexpected Error"
                )
            }
        }
    }

    fun clearCancelMessage() {
        _cancelOrderState.value = _cancelOrderState.value.copy(message = null)
    }

    fun clearOrderMessage() {
        _createOrderState.value = _createOrderState.value.copy(message = null)
    }

    fun clearUpdateStatusState() {
        _updateStatusState.value = UpdateStatusState(success = false, message = null)
    }

    fun setMessage(msg: String) {
        _orderState.value = OrderState(success = false, message = msg)
    }


    fun listOrders() {
        viewModelScope.launch {
            _listOrderState.value = _listOrderState.value.copy(isLoading = true)
            val res = OrderRepository.listOrders()
            if (res?.statuscode == 200) {
                _listOrderState.value = ListOrderState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
            } else {
                _listOrderState.value = ListOrderState(
                    success = false,
                    isLoading = false,
                    message = res?.message ?: "Unexpected Error"
                )
            }
        }
    }


    data class OrderState(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: ProductCategoryList? = null,
        val message: String? = null
    )

    data class CreateOrderState(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: CreateOrderResponse? = null,
        val message: String? = null
    )

    data class CancelOrderState(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: CancelOrderResponse? = null,
        val message: String? = null
    )

    data class ListOrderState(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: List<Order>? = null,
        val message: String? = null
    )

    data class UpdateStatusState(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val message: String? = null
    )
}


class CartViewModel : ScreenModel {

    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    private val _showImage = mutableStateOf(true)
    val showImage: State<Boolean> = _showImage

    init {
        loadCart()
    }

    private fun loadCart() {
        val savedItems = SharedPrefs.Cart.get()
        if (savedItems.isNotEmpty()) {
            val ids = savedItems.map { it.productId }
            try {
                val products = DatabaseHolder.instance.productsQueries
                    .getProductsByGuidsForDis(ids) { product_id, hospital_id, product_name, category_id, unit_id, sales_price, MRP, purchase_price, discount, gst_tax_percentage, product_description, created_at, updated_at, discounted_price ->
                        GetProductsForDis(
                            product_id,
                            hospital_id,
                            product_name,
                            category_id,
                            unit_id,
                            sales_price,
                            MRP,
                            purchase_price,
                            discount,
                            gst_tax_percentage,
                            product_description,
                            created_at,
                            updated_at,
                            discounted_price
                        )
                    }
                    .executeAsList()

                savedItems.forEach { savedItem ->
                    val product = products.find { it.product_id == savedItem.productId }
                    if (product != null) {
                        _cartItems.add(CartItem(product, mutableStateOf(savedItem.quantity)))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveCart() {
        val itemsToSave = _cartItems.map {
            SharedPrefs.Cart.CartPersistenceItem(it.product.product_id ?: "", it.quantity.value)
        }
        SharedPrefs.Cart.save(itemsToSave)
    }

    fun addProduct(product: GetProductsForDis) {
        val existingItem =
            _cartItems.find { it.product.product_id.toString() == product.product_id.toString() }
        if (existingItem != null) {
            existingItem.quantity.value++
        } else {
            _cartItems.add(CartItem(product))
        }
        saveCart()
    }

    fun removeProduct(product: GetProductsForDis) {
        _cartItems.removeAll { it.product.product_id == product.product_id }
        saveCart()
    }

    fun isProductInCart(product: GetProductsForDis): Boolean {
        return _cartItems.any { it.product.product_id == product.product_id }
    }

    fun getTotalProductCount(): Double {
        return _cartItems.sumOf { it.quantity.value }
    }

    fun getAllProducts(): List<CartItem> {
        return _cartItems.toList()
    }

    fun emptyList() {
        _cartItems.clear()
        saveCart()
    }

    fun showImage(): Boolean {
        return _showImage.value
    }

    fun changeShowImage(bool: Boolean) {
        _showImage.value = bool
    }

    fun increaseQuantity(product: GetProductsForDis) {
        addProduct(product)
    }

    fun decreaseQuantity(product: GetProductsForDis) {
        val existingItem = _cartItems.find { it.product.product_id == product.product_id }
        if (existingItem != null) {
            if (existingItem.quantity.value > 1.0) {
                existingItem.quantity.value--
            } else {
                _cartItems.remove(existingItem)
            }
        }
        saveCart()
    }

    fun getProductQuantity(product: GetProductsForDis): Double {
        return _cartItems.find { it.product.product_id == product.product_id }?.quantity?.value ?: 0.0
    }

    fun updateQuantity(product: GetProductsForDis, quantity: Double) {
        val existingItem = _cartItems.find { it.product.product_id == product.product_id }
        if (existingItem != null) {
            if (quantity > 0) {
                existingItem.quantity.value = quantity
            } else {
                _cartItems.remove(existingItem)
            }
        } else if (quantity > 0) {
            _cartItems.add(CartItem(product, mutableStateOf(quantity)))
        }
        saveCart()
    }
}

data class CartItem(
    val product: GetProductsForDis,
    val quantity: MutableState<Double> = mutableStateOf(1.0)
)

data class ProductCartItem(
    val productList: Product,
    val quantity: Double
)
