package decisions4s.persistence.cel

import decisions4s.persistence.DecisionTableDTO
import decisions4s.{DecisionTable, HKD, HitPolicy}
import dev.cel.common.types.{CelType, SimpleType}
import org.scalatest.freespec.AnyFreeSpec

class CelDecisionTableTest extends AnyFreeSpec {

  "test" in {

    case class Input[F[_]](a: F[Int]) derives HKD
    case class Output[F[_]](b: F[Int]) derives HKD

    val dto: DecisionTableDTO = DecisionTableDTO(
      Seq(
        DecisionTableDTO.Rule(
          Map("a" -> "a > 2"),
          Map("b" -> "a + 1"),
          None,
        ),
      ),
      "table1",
    )

    val inputTypes: Input[ToCelType]   = Input(new ToCelType[Int] {
      override def tpe: CelType = SimpleType.INT
    })
    val outputReaders: Output[FromCel] = Output(new FromCel[Int] {
      def read(tpe: CelType): Option[Any => Int] = Option.when(tpe == SimpleType.INT)(x => x.toString.toInt)
    })

    val table: DecisionTable[Input, Output, HitPolicy.First] = CelDecisionTable
      .load(dto, inputTypes, outputReaders, HitPolicy.First)
      .get

    val result1                                               = table.evaluateFirst(Input(1))
    println(result1.output)
    println(result1.makeDiagnosticsString)
    println()

    val result2                                               = table.evaluateFirst(Input(3))
    println(result2.output)
    println(result2.makeDiagnosticsString)
  }

}
