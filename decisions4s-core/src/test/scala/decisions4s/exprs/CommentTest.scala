package decisions4s.exprs

import munit.FunSuite

class CommentTest extends FunSuite {

  test("single line") {
    TestUtils.checkExpression(
      Comment(True, "my comment"),
      true,
      expectedFeelExpr = s"""// my comment
                            |true""".stripMargin,
    )
  }

  test("multi line") {
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
