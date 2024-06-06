package decisions4s.example.provider_routing

import decisions4s.Expr

object IsEEA extends Expr[Country, Boolean] {
  val eeaCountries: Set[Country] = Set(Country("PL"), Country("CH"))

  override def evaluate(in: Country): Boolean = IsEEA.eeaCountries.contains(in)

  override def renderExpression: String = s"isEEA(?)"
}
