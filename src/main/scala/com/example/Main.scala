package com.example

import cats.data.NonEmptyList
import cats.effect.IOApp
import cats.effect.IO
import com.siriusxm.example.cart.{CartError, CartItem, CartService, ShoppingProduct}
import com.siriusxm.example.cart.CartService.TestCartService

import java.util.UUID

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    val cartService = new TestCartService
    for {
      _ <- testPricingCalculation(cartService)
      _ <- testSeveralAddingSameProducts(cartService)
      _ <- testUnableToFindCart(cartService)
      _ <- testUnableToFindPrice(cartService)
    } yield ()
  }

  def testPricingCalculation(cartService: CartService): IO[Unit] = {
    val cartId = UUID.randomUUID()
    val initialCartItems = NonEmptyList.of(CartItem(ShoppingProduct.CornFlakes, 2), CartItem(ShoppingProduct.Weetabix, 1))

    for {
      _ <- cartService.addToCart(cartId, initialCartItems).value
      cartForPriceCheck <- cartService.getCart(cartId).value
      _ <- IO(assert(cartForPriceCheck.map(_.subtotal) == Right(BigDecimal(15.02))))
      _ <- IO(assert(cartForPriceCheck.map(_.tax) == Right(BigDecimal(1.88))))
      _ <- IO(assert(cartForPriceCheck.map(_.total) == Right(BigDecimal(16.80))))
    } yield ()
  }

  def testSeveralAddingSameProducts(cartService: CartService): IO[Unit] = {
    val cartId = UUID.randomUUID()
    val initialCartItems = NonEmptyList.of(CartItem(ShoppingProduct.Cheerios, 2), CartItem(ShoppingProduct.Frosties, 3))

    for {
      _ <- cartService.addToCart(cartId, initialCartItems).value
      moreCartItems = NonEmptyList.of(CartItem(ShoppingProduct.Frosties, 2), CartItem(ShoppingProduct.CornFlakes, 6))
      _ <- cartService.addToCart(cartId, moreCartItems).value
      cartForCheck <- cartService.getCart(cartId).value
      expectedCartItems = NonEmptyList.of(
        CartItem(ShoppingProduct.Cheerios, 2),
        CartItem(ShoppingProduct.Frosties, 5),
        CartItem(ShoppingProduct.CornFlakes, 6)
      )
      _ <- IO(assert(cartForCheck.map(_.items) == Right(expectedCartItems)))
    } yield ()
  }

  def testUnableToFindCart(cartService: CartService): IO[Unit] = {
    val nonExistingCartId = UUID.randomUUID()
    for {
      nonExistingCart <- cartService.getCart(nonExistingCartId).value
      _ <- IO(assert(nonExistingCart == Left(CartError.UnableToFindCart)))
    } yield ()
  }

  def testUnableToFindPrice(cartService: CartService): IO[Unit] = {
    val nonExistingCartId = UUID.randomUUID()
    for {
      nonExistingCart <- cartService.getCart(nonExistingCartId).value
      _ <- IO(assert(nonExistingCart == Left(CartError.UnableToFindPrice)))
    } yield ()
  }

}
