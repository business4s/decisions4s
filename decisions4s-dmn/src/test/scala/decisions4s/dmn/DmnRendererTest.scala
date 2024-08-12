package decisions4s.dmn

import org.scalatest.freespec.AnyFreeSpec

import scala.io.Source

class DmnRendererTest extends AnyFreeSpec {

  "basic" in {

    val dmn = DmnRenderer.render(TestDecision.table)
    val expected = Source.fromURL(getClass.getResource("/test.dmn")).mkString
    assert(dmn.toXML == expected)

  }
}
