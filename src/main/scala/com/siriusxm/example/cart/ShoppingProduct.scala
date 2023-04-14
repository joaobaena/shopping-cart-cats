package com.siriusxm.example.cart

sealed trait ShoppingProduct

object ShoppingProduct {
  case object Cheerios extends ShoppingProduct
  case object CornFlakes extends ShoppingProduct
  case object Frosties extends ShoppingProduct
  case object Shreddies extends ShoppingProduct
  case object Weetabix extends ShoppingProduct
}
