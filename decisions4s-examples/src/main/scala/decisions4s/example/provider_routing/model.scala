package decisions4s.example.provider_routing

case class Country(alpha2: String)

case class Currency(code: String)

object Currency {
  def EUR: Currency = Currency("EUR")
}

sealed trait Provider
object Provider {
  case object AcmeCorp        extends Provider
  case object FooInc          extends Provider
  case object BarLtd          extends Provider
  case object BazCo           extends Provider
  case object JDoeEnterprises extends Provider
}