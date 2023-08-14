package com.example.siriusxm.cart

import cats.data.{EitherT, NonEmptyList}
import cats.effect.{Async, IO, Ref}
import cats.implicits._

import java.util.UUID

trait CartService[F[_]] {
  def getCart(cartId: UUID): EitherT[F, CartError, CurrentCart]

  def addToCart(cartId: UUID, cartItemsToAdd: NonEmptyList[CartItem]): EitherT[F, CartError, Unit]
}

object CartService {
  private type Carts = Map[UUID, CurrentCart]

  class LiveCartService[F[_]: Async](productPriceClient: ProductPriceClient[F], carts: Ref[F, Carts])
      extends CartService[F] {
    def getCart(cartId: UUID): EitherT[F, CartError, CurrentCart] =
      EitherT(
        carts.get
          .map(_.get(cartId).toRight(CartError.UnableToFindCart(cartId)))
      )

    def addToCart(cartId: UUID, cartItemsToAdd: NonEmptyList[CartItem]): EitherT[F, CartError, Unit] =
      for {
        cartOpt                <- EitherT.liftF(carts.get.map(_.get(cartId)))
        currentCartItems        = cartOpt.map(_.items.map(_.asCartItem)).toList
        allItems                = mergeItems(currentCartItems.flatten, cartItemsToAdd)
        updatedPricedCartItems <- allItems.traverse { item =>
                                    productPriceClient
                                      .getPriceForProduct(item.shoppingProduct)
                                      .map(res => PricedCartItem(item.shoppingProduct, res.price, item.amount))
                                  }
        _                      <- EitherT.liftF(carts.updateAndGet(_.updated(cartId, CurrentCart(updatedPricedCartItems))))
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
