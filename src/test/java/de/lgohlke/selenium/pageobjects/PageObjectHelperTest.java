package de.lgohlke.selenium.pageobjects;

import de.lgohlke.junit.DriverService;
import de.lgohlke.selenium.webdriver.DriverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class PageObjectHelperTest {
    private final static String TEST_HTML = "" +
            "<html>" +
            "  <head></head>" +
            "  <body>" +
            "    <span id=\"login\">login</span>" +
            "    <span id=\"password\">password</span>" +
            "  </body>" +
            "</html>";

    @Rule
    public final DriverService   driverService   = new DriverService(DriverType.PHANTOMJS);
    @Rule
    public       TemporaryFolder temporaryFolder = new TemporaryFolder();
    private WebDriver         driver;
    private PageFactoryHelper pageFactoryHelper;

    @Before
    public void beforeEachTest() throws IOException {
        driver = spy(driverService.getDriver());

        File html = temporaryFolder.newFile("index.html");
        Files.write(html.toPath(), TEST_HTML.getBytes());

        driver.get("file://" + html.getAbsolutePath());
        pageFactoryHelper = new PageFactoryHelper(driver);
        Mockito.reset(driver);
    }

    @Test
    public void cleanResilientPresenceCheck() {
        PageObjectHelper pageObjectHelper = new PageObjectHelper(driver);
        LoginPageSomeOk  page             = pageFactoryHelper.initElements(LoginPageSomeOk.class);

        assertThat(pageObjectHelper.isPresent(page.usernameInput)).isTrue();
        assertThat(pageObjectHelper.isPresent(page.passwordInput)).isFalse();
    }

    public static class LoginPageSomeOk extends AbstractPageObject {
        @FindBy(how = How.CSS, using = "#login")
        @ValidatePageObjectOnInit
        private WebElement usernameInput;

        @FindBy(how = How.CSS, using = "#pXssword")
        private WebElement passwordInput;

        public LoginPageSomeOk(WebDriver driver) {
            super(driver);
        }
    }
}
