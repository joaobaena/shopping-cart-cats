package com.example.cart

final case class CurrentCart(items: List[PricedCartItem]) {
  lazy val subtotal: BigDecimal = items.map(_.subtotal).sum
  lazy val tax: BigDecimal      = items.map(_.tax).sum
  lazy val total: BigDecimal    = items.map(_.total).sum
}
