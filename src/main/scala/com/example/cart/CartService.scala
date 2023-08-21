package com.example.cart

import cats.data.NonEmptyList
import cats.effect.{Async, Ref}
import cats.implicits._

import java.util.UUID

trait CartService[F[_]] {
  def getCart(cartId: UUID): F[CurrentCart]

  def addToCart(cartId: UUID, cartItemsToAdd: NonEmptyList[CartItem]): F[Unit]
}

object CartService {
  private type Carts = Map[UUID, CurrentCart]

  class LiveCartService[F[_]: Async](productPriceClient: ProductPriceClient[F], carts: Ref[F, Carts])
      extends CartService[F] {
    def getCart(cartId: UUID): F[CurrentCart] =
      carts.get
        .map(c => c.get(cartId))
        .flatMap {
          case Some(currentCart) => Async[F].pure(currentCart)
          case _                 => Async[F].raiseError(CartError.UnableToFindCart(cartId))
        }

    def addToCart(cartId: UUID, cartItemsToAdd: NonEmptyList[CartItem]): F[Unit] =
      for {
        cartOpt                <- carts.get.map(_.get(cartId))
        currentCartItems        = cartOpt.map(_.items.map(_.asCartItem)).toList
        allItems                = mergeItems(currentCartItems.flatten, cartItemsToAdd)
        updatedPricedCartItems <- allItems.traverse { item =>
                                    productPriceClient
                                      .getPriceForProduct(item.shoppingProduct)
                                      .map(res => PricedCartItem(item.shoppingProduct, res.price, item.amount))
                                  }
        _                      <- carts.updateAndGet(_.updated(cartId, CurrentCart(updatedPricedCartItems)))
      } yield ()

    private def mergeItems(currentItems: List[CartItem], addItems: NonEmptyList[CartItem]): List[CartItem] =
      (addItems.toList ++ currentItems)
        .groupBy(c => c.shoppingProduct)
        .toList
        .map { case (product, cartItems) =>
          CartItem(product, cartItems.map(_.amount).sum)
        }
  }
}
