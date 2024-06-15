package decisions4s.dmn.image

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.*
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.support.ui.WebDriverWait

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.Duration
import javax.imageio.ImageIO
import scala.util.Try

class DmnToImageConverter(customDriver: Option[WebDriver with JavascriptExecutor with TakesScreenshot] = None) {

  private val driver: WebDriver with JavascriptExecutor with TakesScreenshot = customDriver.getOrElse(createDefaultDriver())

  def convertDiagram(dmnXml: String): Try[IArray[Byte]] = Try {
    try {
      openSkeleton()
      loadDmn(dmnXml)
      takesScreenshot()
    } finally {
      // Close the browser
      driver.quit() // TODO
    }
  }

  def openSkeleton(): Unit = {
    // Load the bundled HTML
    val url  = getClass.getResource("/dist/skeleton.html").toString
    driver.get(url)
    // Wait until the page is loaded
    val wait = new WebDriverWait(driver, Duration.ofSeconds(10))
    wait.until((d: WebDriver) => {
      d.asInstanceOf[JavascriptExecutor].executeScript("return document.readyState").toString == "complete"
    })
  }

  private def loadDmn(dmnXml: String): Unit = {
    val loadXMLScript = """window.dmnViewerInterface.openDiagram(arguments[0]);"""
    driver.executeScript(loadXMLScript, dmnXml)
  }

  private def takesScreenshot(): IArray[Byte] = {
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

  private def createDefaultDriver(): WebDriver with JavascriptExecutor with TakesScreenshot = {
    WebDriverManager.chromedriver().setup()
    val options = new ChromeOptions()
    options.addArguments("--headless=new")
    options.addArguments("--window-size=3840,2160")
    options.addArguments("--allow-file-access-from-files")
    new ChromeDriver(options)
  }

}
