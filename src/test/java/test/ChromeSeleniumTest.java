package org.example;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions; // Importar ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collections;

public class ChromeSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");

        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/120.0.0.0 Safari/537.36"
        );

        driver = new ChromeDriver(options);

        wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(15)
        );
    }

    @Test
    public void buscarDocumentacionSelenium() throws Exception {

        driver.get("https://www.google.com/");

        WebElement buscador = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.name("q")
                )
        );

        buscador.sendKeys("Documentación de selenium");
        buscador.sendKeys(Keys.ENTER);

        wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.id("search")
                )
        );

        Thread.sleep(2000);

        tomarCaptura("01-resultados-google.png");

        WebElement enlaceSelenium = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath(
                                "//a[contains(@href,'selenium.dev/documentation')]"
                        )
                )
        );

        enlaceSelenium.click();

        wait.until(
                ExpectedConditions.urlContains(
                        "selenium.dev"
                )
        );

        Thread.sleep(2000);

        tomarCaptura("02-documentacion-selenium.png");

        navegarMenu();
    }

    private void navegarMenu() throws InterruptedException {

        String[] opcionesMenu = {
                "WebDriver",
                "Selenium Manager",
                "Grid"
        };

        for (String opcion : opcionesMenu) {

            try {

                WebElement elemento = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.xpath(
                                        "//a[contains(normalize-space(.), '" +
                                                opcion +
                                                "')]"
                                )
                        )
                );

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});",
                        elemento
                );

                Thread.sleep(1000);

                wait.until(
                        ExpectedConditions.elementToBeClickable(elemento)
                ).click();

                System.out.println(
                        "Navegando a: " + opcion
                );

                Thread.sleep(2000);

            } catch (TimeoutException e) {

                System.out.println(
                        "No se encontró la opción: " + opcion
                );
            }
        }
    }

    private void tomarCaptura(String nombreArchivo)
            throws IOException {

        File captura = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE);

        Path carpeta = Path.of("screenshots");

        Files.createDirectories(carpeta);

        Path destino = carpeta.resolve(nombreArchivo);

        Files.copy(
                captura.toPath(),
                destino,
                StandardCopyOption.REPLACE_EXISTING
        );

        System.out.println(
                "Captura guardada en: "
                        + destino.toAbsolutePath()
        );
    }

    @AfterClass(alwaysRun = true)
    public void cerrarNavegador() {

        if (driver != null) {
            driver.quit();
        }
    }
}