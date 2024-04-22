package decisions4s.example.provider_routing

import decisions4s.Expr

class IsEEA(value: Expr[Country]) extends Expr[Boolean] {
  override def evaluate: Boolean = IsEEA.eeaCountries.contains(value.evaluate)

  override def describe: String = s"isEEA(${value.describe})"
}

object IsEEA {
  val eeaCountries: Set[Country] = Set(Country("PL"), Country("CH"))
  extension (e: Expr[Country]) {
    def isEEA: Expr[Boolean] = IsEEA(e)
  }
}