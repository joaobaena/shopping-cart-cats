package com.siriusxm.example.cart

import cats.data.EitherT
import cats.effect.{Async}
import cats.implicits._
import sttp.client3.circe._
import sttp.client3._
import sttp.client3.armeria.cats.ArmeriaCatsBackend

import scala.util.Random

trait ProductPriceClient[F[_]] {
  def getPriceForProduct(product: ShoppingProduct): EitherT[F, CartError, ShoppingProductPriceResponse]
}

object ProductPriceClient {
  class TestToFailProductPriceClient[F[_]: Async] extends ProductPriceClient[F] {
    def getPriceForProduct(product: ShoppingProduct): EitherT[F, CartError, ShoppingProductPriceResponse] =
      product match {
        case ShoppingProduct.Cheerios =>
          EitherT.leftT[F, ShoppingProductPriceResponse](CartError.UnableToFindPrice(product))
        case p: ShoppingProduct =>
          val price = BigDecimal(Random.between(0.01, 10.00)).setScale(2, BigDecimal.RoundingMode.HALF_UP)
          EitherT.rightT[F, CartError](ShoppingProductPriceResponse(p.toString, price))
      }
  }

  class LiveProductPriceClient[F[_]: Async] extends ProductPriceClient[F] {
    val backend = ArmeriaCatsBackend[F]()

    def getPriceForProduct(product: ShoppingProduct): EitherT[F, CartError, ShoppingProductPriceResponse] = {
      val productName = product.toString.toLowerCase
      val request = basicRequest
        .get(uri"""https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/${productName}.json""")
        .response(asJson[ShoppingProductPriceResponse])
      val result = backend.send(request)
        .map(_.body.leftMap(_ => CartError.UnableToFindPrice(product)).leftWiden[CartError])
      EitherT(result)
    }
  }
}
