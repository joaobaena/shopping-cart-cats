package com.example.siriusxm.cart

final case class CartItem(shoppingProduct: ShoppingProduct, amount: Int) {
  require(amount > 0, "Amount must be greater than zero")
}
