package decisions4s.example.docs

import decisions4s.*

object RulesExample {

  {
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

  {
    // start_nested_structures
    case class Name[F[_]](first: F[String], last: F[String]) derives HKD
    case class Input[F[_]](motherName: F[Name[F]], fatherName: F[Name[F]]) derives HKD
    case class Output[F[_]](childName: F[Name[F]]) derives HKD

    Rule(
      matching = Input(
        motherName = it.catchAll,
        fatherName = wholeInput[Input].fatherName.projection.last === "Smith",
      ),
      output = ctx ?=>
        Output(
          childName = Name[OutputValue](
            wholeInput.fatherName.projection.first,
            wholeInput.motherName.projection.last,
          ),
        ),
    )
    // end_nested_structures
  }

}
