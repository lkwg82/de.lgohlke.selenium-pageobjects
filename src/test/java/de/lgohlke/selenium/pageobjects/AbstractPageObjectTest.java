package de.lgohlke.selenium.pageobjects;

import lombok.Getter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlToBe;

public class AbstractPageObjectTest {
    private final static String TEST_HTML = "" +
            "<html>" +
            "  <head></head>" +
            "  <body>" +
            "    <span id=\"login\">login</span>" +
            "    <span id=\"password\">password</span>" +
            "  </body>" +
            "</html>";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void timeoutExceptionFromWaitForShouldBePropagated() throws IOException {

        File html = temporaryFolder.newFile("index.html");
        Files.write(html.toPath(), TEST_HTML.getBytes());

        EventFiringWebDriver   driver   = new EventFiringWebDriver(new HtmlUnitDriver());
        WebDriverEventListener listener = new WebDriverEventListener();
        driver.register(listener);

        driver.get("file://" + html.getAbsolutePath());
        new PageFactoryHelper(driver).initElements(Element.class);

        assertThat(listener.getExceptions().size()).isEqualTo(1);
        assertThat(listener.getExceptions().get(0)).isInstanceOf(TimeoutException.class);
    }

    public static class WebDriverEventListener extends AbstractWebDriverEventListener {
        @Getter
        private List<Throwable> exceptions = new ArrayList<>();

        @Override
        public void onException(Throwable throwable, WebDriver driver) {
            exceptions.add(throwable);
        }
    }

    public static class Element extends AbstractPageObject {
        public Element(WebDriver driver) {
            super(driver);
        }

        @Override
        public void beforeInit() {
            waitFor(urlToBe("ss"), 1);
        }
    }
}