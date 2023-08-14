package com.example

import cats.data.NonEmptyList
import cats.effect.{IO, IOApp, Ref}
import com.example.siriusxm.cart.CartService.LiveCartService
import com.example.siriusxm.cart.{CartError, CartItem, CartService, CurrentCart, ShoppingProduct}
import com.example.siriusxm.cart.ProductPriceClient.{LiveProductPriceClient, TestToFailProductPriceClient}

import java.util.UUID

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      ref                 <- Ref[IO].of(Map.empty[UUID, CurrentCart])
      cartService          = new LiveCartService(new LiveProductPriceClient[IO], ref)
      testErrorCartService = new LiveCartService(new TestToFailProductPriceClient[IO], ref)
      _                   <- testPricingCalculation(cartService)
      _                   <- testSeveralAddingSameProducts(cartService)
      _                   <- testUnableToFindCart(cartService)
      _                   <- testUnableToFindPrice(testErrorCartService)
      _                   <- IO.println("All tests successfully run")
    } yield ()

  private def testPricingCalculation(cartService: CartService[IO]): IO[Unit] = {
    val cartId           = UUID.randomUUID()
    val initialCartItems =
      NonEmptyList.of(CartItem(ShoppingProduct.CornFlakes, 2), CartItem(ShoppingProduct.Weetabix, 1))

    for {
      _                 <- cartService.addToCart(cartId, initialCartItems).value
      cartForPriceCheck <- cartService.getCart(cartId).value
      _                 <- IO(assert(cartForPriceCheck.map(_.subtotal) == Right(BigDecimal(15.02))))
      _                 <- IO(assert(cartForPriceCheck.map(_.tax) == Right(BigDecimal(1.88))))
      _                 <- IO(assert(cartForPriceCheck.map(_.total) == Right(BigDecimal(16.90))))
    } yield ()
  }

  private def testSeveralAddingSameProducts(cartService: CartService[IO]): IO[Unit] = {
    val cartId           = UUID.randomUUID()
    val initialCartItems = NonEmptyList.of(CartItem(ShoppingProduct.Cheerios, 2), CartItem(ShoppingProduct.Frosties, 3))

    for {
      _                 <- cartService.addToCart(cartId, initialCartItems).value
      moreCartItems      = NonEmptyList.of(CartItem(ShoppingProduct.Frosties, 2), CartItem(ShoppingProduct.Shreddies, 6))
      _                 <- cartService.addToCart(cartId, moreCartItems).value
      cartItemsForCheck <- cartService
                             .getCart(cartId)
                             .value
                             .map(_.map(_.items.map(_.asCartItem).sortBy(_.shoppingProduct.toString)))
      expectedCartItems  = List(
                             CartItem(ShoppingProduct.Cheerios, 2),
                             CartItem(ShoppingProduct.Frosties, 5),
                             CartItem(ShoppingProduct.Shreddies, 6)
                           ).sortBy(_.shoppingProduct.toString)
      _                 <- IO(assert(cartItemsForCheck == Right(expectedCartItems)))
    } yield ()
  }

  private def testUnableToFindCart(cartService: CartService[IO]): IO[Unit] = {
    val nonExistingCartId = UUID.randomUUID()
    for {
      nonExistingCart <- cartService.getCart(nonExistingCartId).value
      _               <- IO(assert(nonExistingCart == Left(CartError.UnableToFindCart(nonExistingCartId))))
    } yield ()
  }

  private def testUnableToFindPrice(cartService: CartService[IO]): IO[Unit] = {
    val cartId    = UUID.randomUUID()
    val cartItems = NonEmptyList.of(CartItem(ShoppingProduct.Cheerios, 2), CartItem(ShoppingProduct.Weetabix, 1))
    for {
      error <- cartService.addToCart(cartId, cartItems).value
      _     <- IO(assert(error == Left(CartError.UnableToFindPrice(ShoppingProduct.Cheerios))))
    } yield ()
  }

}
