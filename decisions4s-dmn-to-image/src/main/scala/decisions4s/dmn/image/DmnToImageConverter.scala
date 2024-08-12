package decisions4s.dmn.image

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.*
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.support.ui.WebDriverWait

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.Duration
import javax.imageio.ImageIO
import scala.util.Try

/** Helper allowing to convert dmn XML into image through rendering in a browser (using dmn-js) and taking a screenshot.
  */
class DmnToImageConverter(customDriver: Option[WebDriver & JavascriptExecutor & TakesScreenshot] = None) extends AutoCloseable {

  private val (driver, shouldClose): (WebDriver & JavascriptExecutor & TakesScreenshot, Boolean) =
    customDriver.map(_ -> false).getOrElse(createDefaultDriver() -> true)

  def convertDiagram(dmnXml: String): Try[IArray[Byte]] = Try {
    openSkeleton()
    loadDmn(dmnXml)
    takeScreenshot()
  }

  private def openSkeleton(): Unit = {
    // Load the bundled HTML
    val url  = getClass.getResource("/generated-web-bundle/index.html").toString
    driver.get(url)
    // Wait until the page is loaded
    val wait = new WebDriverWait(driver, Duration.ofSeconds(10))
    wait.until((d: WebDriver) => {
      d.asInstanceOf[JavascriptExecutor].executeScript("return document.readyState").toString == "complete"
    })
    ()
  }

  private def loadDmn(dmnXml: String): Unit = {
    val loadXMLScript = """window.dmnViewerInterface.openDiagram(arguments[0]);"""
    driver.executeScript(loadXMLScript, dmnXml)
    ()
  }

  private def takeScreenshot(): IArray[Byte] = {
    val screenshotBytes = driver.getScreenshotAs(OutputType.BYTES)
    val dmnTable        = driver.findElement(By.className("tjs-container"))
    val point           = dmnTable.getLocation
    val tableWidth      = dmnTable.getSize.getWidth
    val tableHeight     = dmnTable.getSize.getHeight
    val fullImg         = ImageIO.read(ByteArrayInputStream(screenshotBytes))
    val subScreenshot   = fullImg.getSubimage(point.getX, point.getY, tableWidth, tableHeight)
    val os              = new ByteArrayOutputStream()
    ImageIO.write(subScreenshot, "png", os)
    IArray.unsafeFromArray(os.toByteArray)
  }

  private def createDefaultDriver(): WebDriver & JavascriptExecutor & TakesScreenshot = {
    WebDriverManager.chromedriver().setup()
    val options = new ChromeOptions()
    // "--headless" seems to ignore "--windows-size"
    options.addArguments("--headless=new")
    // allows fully rendering big tables. Could potentially be bumped even higher
    options.addArguments("--window-size=3840,2160")
    // without this accessing css and js results in CORS error
    options.addArguments("--allow-file-access-from-files")
    new ChromeDriver(options)
  }

  override def close(): Unit = if (shouldClose) driver.quit()
}
