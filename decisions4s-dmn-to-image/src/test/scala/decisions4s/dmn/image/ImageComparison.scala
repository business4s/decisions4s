package decisions4s.dmn.image

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO

object ImageComparison {

  // gives a value between 0 and 100. 0 means no difference, 100 means very different.
  def getDifferencePercent(img1: IArray[Byte], img2: IArray[Byte]): Double = {
    getDifferencePercent(
      ImageIO.read(new ByteArrayInputStream(img1.toArray)),
      ImageIO.read(new ByteArrayInputStream(img2.toArray)),
    )
  }

  private def getDifferencePercent(img1: BufferedImage, img2: BufferedImage): Double = {
    val width = img1.getWidth
    val height = img1.getHeight
    var diff = 0L

    for (y <- 0 until height) {
      for (x <- 0 until width) {
        val pixel1 = img1.getRGB(x, y)
        val pixel2 = img2.getRGB(x, y)
        diff += pixelDiff(pixel1, pixel2)
      }
    }

    val maxDiff = 3L * 255 * width * height
    100.0 * diff / maxDiff
  }

  private def pixelDiff(pixel1: Int, pixel2: Int): Int = {
    val r1 = (pixel1 >> 16) & 0xff
    val g1 = (pixel1 >> 8) & 0xff
    val b1 = pixel1 & 0xff
    val r2 = (pixel2 >> 16) & 0xff
    val g2 = (pixel2 >> 8) & 0xff
    val b2 = pixel2 & 0xff

    math.abs(r1 - r2) + math.abs(g1 - g2) + math.abs(b1 - b2)
  }

}
