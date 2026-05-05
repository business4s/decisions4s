package decisions4s.dmn

import org.camunda.bpm.model.dmn.{Dmn, DmnModelInstance}

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.zip.{Deflater, DeflaterOutputStream}

case class DMNModel(raw: DmnModelInstance) {
  def toXML: String = Dmn.convertToString(raw)

  def shareUrl(viewerUrl: String = DMNModel.DefaultViewerUrl): String = {
    val deflated = DMNModel.deflate(toXML.getBytes(StandardCharsets.UTF_8))
    val encoded  = Base64.getUrlEncoder.withoutPadding.encodeToString(deflated)
    s"$viewerUrl${DMNModel.HashPrefix}$encoded"
  }
}

object DMNModel {

  val DefaultViewerUrl = "https://business4s.github.io/decisions4s/dmn-viewer/"
  val HashPrefix       = "#dmn="

  private def deflate(input: Array[Byte]): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val dos = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION))
    try dos.write(input)
    finally dos.close()
    out.toByteArray
  }
}
