package com.siriusxm.example.cart

import scala.math.BigDecimal.RoundingMode

final case class PricedCartItem(shoppingProduct: ShoppingProduct, basePrice: BigDecimal, amount: Int) {
  require(amount > 0, "Amount must be greater than zero")
  lazy val subtotal: BigDecimal = (amount * basePrice).setScale(2, RoundingMode.CEILING)
  lazy val tax: BigDecimal      = (subtotal * 0.125).setScale(2, RoundingMode.CEILING)
  lazy val total: BigDecimal    = subtotal + tax
  lazy val asCartItem: CartItem = CartItem(shoppingProduct, amount)
}
