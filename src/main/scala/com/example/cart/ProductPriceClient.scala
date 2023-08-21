package com.example.cart

import cats.effect.Async
import cats.implicits._
import sttp.client3.circe._
import sttp.client3._
import sttp.client3.armeria.cats.ArmeriaCatsBackend

import scala.util.Random

trait ProductPriceClient[F[_]] {
  def getPriceForProduct(product: ShoppingProduct): F[ShoppingProductPriceResponse]
}

object ProductPriceClient {
  class TestToFailProductPriceClient[F[_]: Async] extends ProductPriceClient[F] {
    def getPriceForProduct(product: ShoppingProduct): F[ShoppingProductPriceResponse] =
      product match {
        case ShoppingProduct.Cheerios =>
          Async[F].raiseError(CartError.UnableToFindPrice(product))
        case p: ShoppingProduct       =>
          val price = BigDecimal(Random.between(0.01, 10.00)).setScale(2, BigDecimal.RoundingMode.HALF_UP)
          Async[F].pure(ShoppingProductPriceResponse(p.toString, price))
      }
  }

  class LiveProductPriceClient[F[_]: Async] extends ProductPriceClient[F] {
    private val backend = ArmeriaCatsBackend[F]()

    def getPriceForProduct(product: ShoppingProduct): F[ShoppingProductPriceResponse] = {
      val productName = product.toString.toLowerCase
      val request     = basicRequest
        .get(uri"""https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/${productName}.json""")
        .response(asJson[ShoppingProductPriceResponse])
      backend
        .send(request)
        .map(_.body.leftMap(_ => CartError.UnableToFindPrice(product)).leftWiden[CartError])
        .flatMap {
          case Right(value) => Async[F].pure(value)
          case Left(error)  => Async[F].raiseError(error)
        }
    }
  }
}
