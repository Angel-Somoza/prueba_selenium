package test;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class ChromeSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String DIR = "screenshots";

    private static final String[] MENU = {
            "m-documentation",
            "m-documentationoverview",
            "m-documentationwebdriver",
            "m-documentationselenium_manager",
            "m-documentationgrid",
            "m-documentationie_driver_server",
            "m-documentationide",
            "m-documentationtest_practices",
            "m-documentationlegacy",
            "m-documentationwarnings",
            "m-documentationabout"
    };

    @BeforeClass
    public void setUp() throws Exception {
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--start-maximized", "--disable-notifications",
                "--disable-blink-features=AutomationControlled",
                "--no-first-run", "--no-default-browser-check",
                "--disable-extensions", "--disable-popup-blocking",
                "--disable-infobars", "--disable-dev-shm-usage", "--disable-gpu",
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        o.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        o.setExperimentalOption("useAutomationExtension", false);
        o.setPageLoadStrategy(PageLoadStrategy.EAGER);

        driver = new ChromeDriver(o);
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        File carpeta = new File(DIR);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File gitkeep = new File(carpeta, ".gitkeep");
        if (!gitkeep.exists()) {
            gitkeep.createNewFile();
        }

        File gitignore = new File(carpeta, ".gitignore");
        if (!gitignore.exists()) {
            FileWriter fw = new FileWriter(gitignore);
            fw.write("*\n!.gitkeep\n!.gitignore\n");
            fw.close();
        }
    }

    @Test
    public void test() throws Exception {
        driver.get("https://www.google.com");

        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(., 'Aceptar') or contains(., 'Accept') or contains(., 'Acepto')]")))
                    .click();
        } catch (Exception ignored) {
        }

        shot("01_google_home.png");

        WebElement q = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        q.sendKeys("Documentacion de selenium", Keys.ENTER);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
        Thread.sleep(2000);
        shot("02_resultados_busqueda.png");

        WebElement link = findSeleniumLink();
        if (link == null) throw new RuntimeException("sin enlace");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", link);
        Thread.sleep(800);

        try {
            link.click();
        } catch (Exception e) {
            driver.get(link.getAttribute("href"));
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Thread.sleep(2500);
        shot("03_documentacion_selenium.png");

        int i = 4;
        for (String id : MENU) {
            List<WebElement> found = driver.findElements(
                    By.xpath("//li[@id='" + id + "-li']/a[@id='" + id + "']"));
            if (found.isEmpty())
                found = driver.findElements(By.xpath("//a[@id='" + id + "']"));
            if (found.isEmpty()) {
                i++;
                continue;
            }

            WebElement a = found.get(0);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", a);
            Thread.sleep(300);

            try {
                a.click();
            } catch (Exception e) {
                driver.get(a.getAttribute("href"));
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(1500);

            String name = id.replace("m-documentation", "").replaceAll("^_", "");
            if (name.isEmpty()) name = "documentation";
            shot(String.format("%02d_menu_%s.png", i, name));
            i++;
        }
    }

    private WebElement findSeleniumLink() {
        String[] xp = {
                "//a[contains(@href,'selenium.dev/documentation')]",
                "//a[contains(@href,'selenium.dev')]",
                "//cite[contains(.,'selenium.dev')]/ancestor::a[1]",
                "//div[@id='search']//h3/ancestor::a[1]"
        };
        for (String x : xp) {
            List<WebElement> l = driver.findElements(By.xpath(x));
            if (!l.isEmpty()) return l.get(0);
        }
        return null;
    }

    private void shot(String name) throws Exception {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileHandler.copy(src, new File(DIR + File.separator + name));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}