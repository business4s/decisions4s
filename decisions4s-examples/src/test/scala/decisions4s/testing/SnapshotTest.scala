package decisions4s.testing

import org.scalatest.Assertions

import java.nio.file.{Files, Paths}

object SnapshotTest {

  private val testResourcesPath = Paths
    .get(sourcecode.File()) // decisions4s-examples/src/test/scala/decisions4s/testing/SnapshotTest.scala
    .getParent              // decisions4s-examples/src/test/scala/decisions4s/testing
    .getParent              // decisions4s-examples/src/test/scala/decisions4s
    .getParent              // decisions4s-examples/src/test/scala
    .getParent              // decisions4s-examples/src/test
    .resolve("resources")

  def testSnapshot(content: String, path: String) = {
    val filePath    = testResourcesPath.resolve(path)
    val existingOpt = Option.when(Files.exists(filePath)) {
      Files.readString(testResourcesPath.resolve(path))
    }

    val isOk = existingOpt.contains(content)

    if (!isOk) {
      Files.createDirectories(filePath.getParent)
      Files.writeString(filePath, content)
      // file path iso here so you can quickly navigate to the file
      Assertions.fail(s"Snapshot ${path} was not matching. A new value has been written to ${filePath}.")
    }
  }

}
