package decisions4s.dmn.image

import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.{Files, Path}

class DmnToImageConverterTest extends AnyFreeSpec {

  "basic" in {
      val converter = new DmnToImageConverter()
      val xml = scala.io.Source.fromResource("test.dmn").mkString
      val imageBytes = converter.convertDiagram(xml).get
      Files.write(Path.of("outputImage.png"), imageBytes.toArray)


  }

}
