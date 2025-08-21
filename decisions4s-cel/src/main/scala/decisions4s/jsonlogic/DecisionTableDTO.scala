package decisions4s.jsonlogic

import decisions4s.HitPolicy
import decisions4s.jsonlogic.DecisionTableDTO.given
import io.circe.Codec

case class DecisionTableDTO(
    rules: Seq[DecisionTableDTO.Rule],
    name: String,
    hitPolicy: HitPolicy,
) derives Codec

object DecisionTableDTO {

  given Codec[HitPolicy] = Codec.derived

  case class Rule(inputs: Map[String, String], outputs: Map[String, String], annotation: Option[String]) derives Codec
}
