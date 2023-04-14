package com.siriusxm.example.cart

final case class CartItem(shoppingProduct: ShoppingProduct, amount: Int)

object CartItem {
  def create(shoppingProduct: ShoppingProduct, amount: Int): Option[CartItem] =
    if (amount > 0) Some(CartItem(shoppingProduct, amount))
    else None
}
