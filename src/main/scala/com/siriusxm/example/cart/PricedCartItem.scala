package com.siriusxm.example.cart

final case class PricedCartItem(shoppingProduct: ShoppingProduct, basePrice: BigDecimal, amount: Int)
