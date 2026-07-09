package com.example.sportsgear.navigation
const val ROUTE_LOGIN = "login"
const val ROUTE_HOME = "home"
const val ROUTE_REGISTER = "registerScreen"
const val ROUTE_SPLASH = "splash"
const val ROUTE_UPDATE_PRODUCT = "updateproduct"
const val ROUTE_VIEW_PRODUCTS = "viewproduct"
const val ROUTE_ADD_PRODUCT = "addproducts"
const val ROUTE_CART = "cart"
const val ROUTE_EDIT_PROFILE = "editprofile"
const val ROUTE_EDITPRODUCT = "editprodduct/{productId}"
const val ROUTE_ORDER = "orderhistory"
const val ROUTE_PROFILE = "profile"
const val ROUTE_SUCCESS = "successscreen"
const val ROUTE_CHECKOUT = "checkoutscreen"
const val ROUTE_DEBUG = "debugscreen"
const val ROUTE_STARTER = "Startscreen"
const val ROUTE_PAYMENT = "paymentScreen"
const val ROUTE_EDIT_CARTPRODUCT = "editcartproduct/{productId}"
const val ROUTE_CATEGORY = "category/{categoryName}"
const val ROUTE_ADMIN_DASHBOARD= "AdminDashboard"
const val ROUTE_PRODUCT_DETAIL = "product_detail/{productId}"
fun getCategoryRoute(categoryName: String): String {
    return "category/$categoryName"
}
// in your navigation routes file, next to getCategoryRoute
fun getProductDetailRoute(productId: String): String {
    return "product_detail/$productId"
}
fun getEditCartProductRoute(productId: String): String {
    return "editcartproduct/$productId"
}

