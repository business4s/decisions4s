package decisions4s.example.docs

import decisions4s.{Expr, Rule, it, wholeInput}
import decisions4s.*

object RulesExample {

  case class Input[F[_]](a: F[Int], b: F[Int])
  case class Output[F[_]](c: F[Int])

  // start_whole_input
  Rule(
    matching = Input(
      a = it.equalsTo(wholeInput.b),
      b = it.catchAll,
    ),
    output = Output(
      c = wholeInput.a + wholeInput.b,
    ),
  )
  // end_whole_input

}
