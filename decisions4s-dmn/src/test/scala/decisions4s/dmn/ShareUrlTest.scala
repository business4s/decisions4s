package decisions4s.dmn

import org.camunda.bpm.model.dmn.Dmn
import org.scalatest.freespec.AnyFreeSpec

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.zip.InflaterInputStream

class ShareUrlTest extends AnyFreeSpec {

  private lazy val model: DMNModel = DMNModel(Dmn.readModelFromStream(getClass.getResourceAsStream("/test.dmn")))

  "shareUrl emits a URL whose payload round-trips back to the original DMN XML" in {
    val url    = model.shareUrl()
    val prefix = DMNModel.DefaultViewerUrl + DMNModel.HashPrefix
    assert(url.startsWith(prefix))

    val recovered = inflateFromShareUrl(url)
    assert(recovered == model.toXML)
  }

  "shareUrl honors a custom viewer URL" in {
    val custom = "https://example.test/viewer/"
    val url    = model.shareUrl(custom)
    assert(url.startsWith(custom + DMNModel.HashPrefix))
  }

  "the encoded payload uses URL-safe base64 with no padding" in {
    val payload = stripPrefix(model.shareUrl())
    assert(!payload.contains('+'))
    assert(!payload.contains('/'))
    assert(!payload.contains('='))
  }

  private def stripPrefix(url: String): String =
    url.substring(url.indexOf(DMNModel.HashPrefix) + DMNModel.HashPrefix.length)

  private def inflateFromShareUrl(url: String): String = {
    val deflated = Base64.getUrlDecoder.decode(stripPrefix(url))
    val iis      = new InflaterInputStream(new ByteArrayInputStream(deflated))
    new String(iis.readAllBytes(), StandardCharsets.UTF_8)
  }
}
