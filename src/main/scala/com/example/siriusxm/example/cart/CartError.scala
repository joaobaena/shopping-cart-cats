package com.siriusxm.example.cart

import java.util.UUID

sealed trait CartError

object CartError {
  final case class UnableToFindCart(cartId: UUID) extends CartError
  final case class UnableToFindPrice(shoppingProduct: ShoppingProduct) extends CartError
}
