package decisions4s

import decisions4s.exprs.*
import decisions4s.exprs.UnaryTest.{Compare, Or}

import scala.annotation.targetName

// Syntax for defining predicates
object it {

  def satisfies[T](f: Expr[T] => Expr[Boolean]): UnaryTest[T] = UnaryTest.WithValue(f)

  @targetName("equalsToOp")
  def ===[T](value: T)(using LiteralShow[T]): UnaryTest[T]            = UnaryTest.EqualTo(Literal(value))
  def equalsTo[T](value: T)(using LiteralShow[T]): UnaryTest[T]       = UnaryTest.EqualTo(Literal(value))
  def equalsTo[T](value: Expr[T])(using LiteralShow[T]): UnaryTest[T] = UnaryTest.EqualTo(value)

  def equalsAnyOf[T](values: T*)(using LiteralShow[T]): UnaryTest[T] = UnaryTest.Or(values.map(it.equalsTo))
  def equalsAnyOf[T](values: Expr[Iterable[T]]): UnaryTest[T]        = UnaryTest.OneOf(values)

  def >[T](value: T)(using LiteralShow[T], Ordering[T]): UnaryTest[T]  = Compare(Compare.Sign.`>`, Literal(value))
  def >[T](value: Expr[T])(using Ordering[T]): UnaryTest[T]            = Compare(Compare.Sign.`>`, value)
  def >=[T](value: T)(using LiteralShow[T], Ordering[T]): UnaryTest[T] = Compare(Compare.Sign.`>=`, Literal(value))
  def >=[T](value: Expr[T])(using Ordering[T]): UnaryTest[T]           = Compare(Compare.Sign.`>=`, value)

  def <[T](value: T)(using LiteralShow[T], Ordering[T]): UnaryTest[T]  = Compare(Compare.Sign.`<`, Literal(value))
  def <[T](value: Expr[T])(using Ordering[T]): UnaryTest[T]            = Compare(Compare.Sign.`<`, value)
  def <=[T](value: T)(using LiteralShow[T], Ordering[T]): UnaryTest[T] = Compare(Compare.Sign.`<=`, Literal(value))
  def <=[T](value: Expr[T])(using Ordering[T]): UnaryTest[T]           = Compare(Compare.Sign.`<=`, value)

  def catchAll[T]: UnaryTest[T] = UnaryTest.CatchAll

  def isTrue: Expr[Boolean]       = True
  def isFalse: UnaryTest[Boolean] = UnaryTest.EqualTo(Literal(false))

//  def isOneOf[T](values: Iterable[T]): UnaryTest[Boolean] = UnaryTest.OneOf(values)
}
