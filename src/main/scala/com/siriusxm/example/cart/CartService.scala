package com.siriusxm.example.cart

import cats.data.{EitherT, NonEmptyList}
import cats.effect.IO

import java.util.UUID

trait CartService {
  def getCart(cartId: UUID): EitherT[IO, CartError, CurrentCart]
  def addToCart(cartId: UUID, productsToAdd: NonEmptyList[CartItem]): EitherT[IO, CartError, Unit]
}

object CartService {
  class TestCartService extends CartService {
    def getCart(cartId: UUID): EitherT[IO, CartError, CurrentCart] = ???

    def addToCart(cartId: UUID, productsToAdd: NonEmptyList[CartItem]): EitherT[IO, CartError, Unit] = ???
  }
}
