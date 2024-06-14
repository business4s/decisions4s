package decisions4s.dmn.image

import org.graalvm.polyglot.*
import org.graalvm.polyglot.io.IOAccess

import java.io.File
import scala.jdk.CollectionConverters.*

object DmnToImageApp extends App {
  val options = Map(
    "js.commonjs-require" -> "true",
    "js.commonjs-require-cwd" -> "/Users/krever/Projects/priv/decisions4s/decisions4s-dmn-to-image/js-bundle"
  )
  val context = Context
    .newBuilder("js")
    .allowExperimentalOptions(true)
    .allowIO(IOAccess.ALL)
    .options(options.asJava)
    .build();
//  val src     = Source.newBuilder("js", new File("/Users/krever/Projects/priv/decisions4s/decisions4s-dmn-to-image/js-bundle/dist/bundle.js")).build()
  val xml     = scala.io.Source.fromFile("/Users/krever/Projects/priv/decisions4s/banking-provider-decision.dmn")
  context.getPolyglotBindings.putMember("dmnXml", xml)
  val jsCode  =
    """
      |//const dmnXml = Polyglot.import('dmnXml');
      |const dmnToImage = require('./dist/bundle.js');
      |dmnToImage.convertDmnToImage(dmnXml);
      |""".stripMargin
//
  context.eval("js", "globalThis.global = {};")
//  val result = context.eval(src).execute(1)
  val result = context.eval("js", jsCode)
  println(result.asString())
}
