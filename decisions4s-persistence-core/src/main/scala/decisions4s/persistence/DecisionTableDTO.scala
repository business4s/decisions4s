package decisions4s.persistence

import io.circe.Codec

case class DecisionTableDTO(
    rules: Seq[DecisionTableDTO.Rule],
    name: String,
) derives Codec

object DecisionTableDTO {

  case class Rule(inputs: Map[String, String], outputs: Map[String, String], annotation: Option[String]) derives Codec
}
