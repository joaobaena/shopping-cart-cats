package com.siriusxm.example.cart

final case class CurrentCart(items: List[PricedCartItem]) {
  lazy val subtotal: BigDecimal = ???
  lazy val tax: BigDecimal = ???
  lazy val total: BigDecimal = ???
}
