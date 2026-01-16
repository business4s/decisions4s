package decisions4s.persistence

import decisions4s.HitPolicy

import io.circe.Codec

case class DecisionTableDTO(
    rules: Seq[DecisionTableDTO.Rule],
    name: String,
) derives Codec

object DecisionTableDTO {

  given Codec[HitPolicy] = Codec.derived

  case class Rule(inputs: Map[String, String], outputs: Map[String, String], annotation: Option[String]) derives Codec
}
