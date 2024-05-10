package decisions4s.example.provider_routing

import decisions4s.LiteralShow

case class Country(alpha2: String)

case class Currency(code: String)

object Currency {
  def EUR: Currency        = Currency("EUR")
  def PLN: Currency        = Currency("PLN")
  def CHF: Currency        = Currency("CHF")
  given LiteralShow[Currency] = _.code
}

sealed trait Provider
object Provider {
  case object AcmeCorp        extends Provider
  case object FooInc          extends Provider
  case object BarLtd          extends Provider
  case object BazCo           extends Provider
  case object JDoeEnterprises extends Provider

  given LiteralShow[Provider] = _.toString
}
