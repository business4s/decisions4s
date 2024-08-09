package decisions4s.exprs

import org.scalatest.freespec.AnyFreeSpec
class CommentTest extends AnyFreeSpec {

  "single line" in {
    TestUtils.checkExpression(
      Comment(True, "my comment"),
      true,
      expectedFeelExpr = s"""// my comment
                            |true""".stripMargin,
    )
  }

  "multi line" in {
    TestUtils.checkExpression(
      Comment(
        True,
        """line 1
          |line 2""".stripMargin,
      ),
      true,
      expectedFeelExpr = s"""/*
                            | * line 1
                            | * line 2
                            | */
                            |true""".stripMargin,
    )
  }

}
