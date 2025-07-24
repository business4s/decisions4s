package decisions4s.dmn.image

import org.scalatest.freespec.AnyFreeSpec

import java.io.{File, FileOutputStream}
import java.net.URLClassLoader
import java.nio.file.{Files, Path}
import java.util.jar.{JarEntry, JarOutputStream}

class WebBundleTest extends AnyFreeSpec {

  "unpack files from jar" in {
    val jarFile     = createTestJar()
    val classLoader = new URLClassLoader(Array(jarFile.toURI.toURL), this.getClass.getClassLoader)

    val webBundle = WebBundle.fromResources("test", classLoader)
    assert(!webBundle.indexHtmlUri.toString.contains("jar"))
    val extracted = Path.of(webBundle.indexHtmlUri)
    assert(Files.exists(extracted))
    assert(Files.readString(extracted).contains("Hello from test JAR"))
  }

  private def createTestJar(): File = {
    val jarFile = File.createTempFile("webbundle-test", ".jar")
    jarFile.deleteOnExit()

    val jarOut = new JarOutputStream(new FileOutputStream(jarFile))
    try {
      def addEntry(pathInJar: String, content: Option[String] = None): Unit = {
        val entry = new JarEntry(pathInJar)
        entry.setTime(System.currentTimeMillis())
        jarOut.putNextEntry(entry)
        content.foreach { c => jarOut.write(c.getBytes("UTF-8")) }
        jarOut.closeEntry()
      }

      addEntry("test/") // ← this is the missing directory entry
      addEntry("test/index.html", Some("<html><body>Hello from test JAR</body></html>"))
    } finally {
      jarOut.close()
    }

    jarFile
  }
}
