package com.example.siriusxm.cart

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class ShoppingProductPriceResponse(title: String, price: BigDecimal)

object ShoppingProductPriceResponse {
  implicit val decoder: Decoder[ShoppingProductPriceResponse] = deriveDecoder
}
