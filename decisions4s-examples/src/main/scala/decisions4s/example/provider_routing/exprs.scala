package decisions4s.example.provider_routing

import decisions4s.Expr

case class IsEEA(arg: Expr[Country]) extends Expr[Boolean] {

  override def evaluate: Boolean = IsEEA.eeaCountries.contains(arg.evaluate)

  override def renderExpression: String = s"isEEA(${arg.renderExpression})"
}

object IsEEA {
  val eeaCountries: Set[Country] = Set(Country("PL"), Country("CH"))
}
