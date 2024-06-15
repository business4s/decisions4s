package decisions4s.dmn.image

import org.scalatest.freespec.AnyFreeSpec

import scala.util.chaining.scalaUtilChainingOps

class DmnToImageConverterTest extends AnyFreeSpec {

  "basic" in {
    val converter      = new DmnToImageConverter()
    val xml            = scala.io.Source.fromResource("test.dmn").mkString
    val generatedImage = converter.convertDiagram(xml).get
    val expectedImage  = getClass.getResourceAsStream("/test.png").readAllBytes().pipe(IArray.unsafeFromArray)
    val diffPercent    = ImageComparison.getDifferencePercent(generatedImage, expectedImage)
    // we allow for 1% difference
    assert(diffPercent <= 1)
  }

}
