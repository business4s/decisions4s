package decisions4s.dmn.image

import java.net.URI
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileSystemNotFoundException, FileSystems, FileVisitResult, Files, Path, Paths, SimpleFileVisitor, StandardCopyOption}
import java.util.Collections

case class WebBundle(indexHtmlUri: URI)

object WebBundle {

  def fromResources(resourcePath: String, classLoader: ClassLoader = getClass.getClassLoader): WebBundle = {
    val temp = Files.createTempDirectory("dmn-web-bundle")
    temp.toFile.deleteOnExit()
    copyFromResource(resourcePath, temp, classLoader)
    val url  = temp.resolve("index.html").toUri
    WebBundle(url)
  }

  /** Copy a directory (and all sub‐entries) out of the JAR into `target` */
  private def copyFromResource(source: String, target: Path, classLoader: ClassLoader): Unit = {
    // Grab a URI pointing into the JAR
    val sourcePath = classLoader.getResource(source).toURI
    withPath(sourcePath) { jarPath =>
      val _ = Files.walkFileTree(
        jarPath,
        new SimpleFileVisitor[Path]() {
          override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
            val rel = jarPath.relativize(dir).toString
            Files.createDirectories(target.resolve(rel))
            FileVisitResult.CONTINUE
          }

          override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
            val rel = jarPath.relativize(file).toString
            Files.copy(file, target.resolve(rel), StandardCopyOption.REPLACE_EXISTING)
            FileVisitResult.CONTINUE
          }
        },
      )
    }
  }

  private def withPath[T](resPath: URI)(thunk: Path => T): T = {
    val (effectivePath, fsOpt) =
      try {
        // Direct file-system path (IDE or exploded resources)
        (Paths.get(resPath), None)
      } catch {
        case _: FileSystemNotFoundException =>
          // Resource inside a JAR: mount a new FileSystem
          val fs   = FileSystems.newFileSystem(resPath, Collections.emptyMap[String, String]())
          val path = fs.provider().getPath(resPath)
          (path, Some(fs))
      }

    try {
      thunk(effectivePath)
    } finally {
      fsOpt.foreach(_.close())
    }
  }

}
