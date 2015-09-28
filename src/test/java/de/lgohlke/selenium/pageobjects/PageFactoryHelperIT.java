package de.lgohlke.selenium.pageobjects;

import com.googlecode.junittoolbox.ParallelRunner;
import de.lgohlke.junit.HttpServerFromResource;
import de.lgohlke.selenium.webdriver.DriverType;
import de.lgohlke.selenium.webdriver.junit.DriverService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(ParallelRunner.class)
@Slf4j
public class PageFactoryHelperIT {
    @Rule
    public final DriverService          driverService          = new DriverService(DriverType.PHANTOMJS);
    @Rule
    public       TemporaryFolder        temporaryFolder        = new TemporaryFolder();
    @Rule
    public       HttpServerFromResource httpServerFromResource = new HttpServerFromResource("/");

    private WebDriver         driver;
    private PageFactoryHelper pageFactoryHelper;

    @Before
    public void beforeEachTest() throws IOException {
        driver = spy(driverService.getDriver());
        pageFactoryHelper = new PageFactoryHelper(driver);
        Mockito.reset(driver);
    }

    private String url(String path) {
        return "http://localhost:" + httpServerFromResource.getPort() + path;
    }

    @Test
    public void testFindingValidateAnnotations() {
        driver.get(url("/PageFactoryHelperTest.html"));

        LoginPage page = pageFactoryHelper.initElements(LoginPage.class);

        assertThat(page.credentials()).isEqualTo("login password");
    }

    @Test
    public void testBeforeInit() {
        driver.get(url("/PageFactoryHelperTest.html"));

        LoginPage page = pageFactoryHelper.initElements(LoginPage.class);

        assertThat(page.isInitialized()).isTrue();
    }

    @Test
    public void testOrderOfHierarchicalBeforeInit() {
        driver.get(url("/PageFactoryHelperTest.html"));

        ParentPage page = pageFactoryHelper.initElements(ParentPage.class);

        assertThat(page.getTimestamp()).isGreaterThan(page.getMenu().getTimestamp());
    }

    @Test
    public void initFieldsWhichArePageObjects() {
        driver.get(url("/PageFactoryHelperTest.html"));

        LoginPage page = pageFactoryHelper.initElements(LoginPage.class);

        assertThat(page.getMenu()).isNotNull();
        assertThat(page.getMenu().getMenuText()).isEqualTo("menu");
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindingValidateAnnotationsAndDetectErrors() {
        driver.get(url("/PageFactoryHelperTest.html"));

        pageFactoryHelper.initElements(LoginPageFail.class);
    }

    @Test
    public void shouldNotOpenLocationWhenLocationIsEmpty() {
        pageFactoryHelper.initElements(PageNoLocation.class);

        verify(driver, times(0)).get(any(String.class));
    }

    @Test
    public void shouldFlatInitElements() {
        driver.get(url("/PageFactoryHelperTest.html"));
        ParentPage page = pageFactoryHelper.initElements(ParentPage.class, true);

        assertThat(page.getMenu()).isNull();
    }

    @Test
    public void shouldOpenLocationWhenLocationIsNotEmpty() {
        pageFactoryHelper.initElements(PageWithLocation.class);

        verify(driver, times(1)).get(any(String.class));
    }

    @Test
    public void shouldNotOpenLocationAnotherTimeIfItIsAlreadyOpened() {
        pageFactoryHelper.initElements(PageWithLocation.class);
        pageFactoryHelper.initElements(PageWithLocation.class);

        verify(driver, times(1)).get(any(String.class));
    }

    @Test
    public void shouldOpenRelativeLocation() {
        driver.get(url("/PageFactoryHelperTest.html"));
        Mockito.reset(driver);

        pageFactoryHelper.initElements(PageRelativeLocation.class);

        String location = new PageRelativeLocation(driver).getLocation();
        verify(driver, times(1)).get(url(location));
    }

    public static class PageRelativeLocation extends AbstractPageObject implements Page {
        public PageRelativeLocation(WebDriver driver) {
            super(driver);
        }

        @Override
        public String getLocation() {
            return "/index.html";
        }
    }

    public static class PageNoLocation extends AbstractPageObject implements Page {
        public PageNoLocation(WebDriver driver) {
            super(driver);
        }

        @Override
        public String getLocation() {
            return "";
        }
    }

    public static class PageWithLocation extends AbstractPageObject implements Page {
        @FindBy(how = How.CSS, using = "body")
        @ValidatePageObjectOnInit
        private WebElement directoryTable;

        public PageWithLocation(WebDriver driver) {
            super(driver);
        }

        @Override
        public String getLocation() {
            return "http://www.google.de/";
        }
    }

    public static class ParentPage extends AbstractPageObject {
        @FindBy(how = How.CSS, using = "#login")
        @ValidatePageObjectOnInit
        private WebElement usernameInput;
        @Getter
        private PageMenu   menu;

        @Getter
        private long timestamp;

        public ParentPage(WebDriver driver) {
            super(driver);
        }

        @Override
        public void beforeInit() {
            timestamp = System.nanoTime();
        }
    }

    public static class PageMenu extends AbstractPageObject {
        @Getter
        private long timestamp;

        public PageMenu(WebDriver driver) {
            super(driver);
        }

        @Override
        public void beforeInit() {
            timestamp = System.nanoTime();
        }
    }

    public static class LoginPage implements PageObject {
        @FindBy(how = How.CSS, using = "#login")
        @ValidatePageObjectOnInit
        private WebElement usernameInput;

        @FindBy(how = How.CSS, using = "#password")
        @ValidatePageObjectOnInit
        private WebElement passwordInput;

        @Getter
        private Menu menu;

        @Getter
        private boolean initialized;

        public void beforeInit() {
            initialized = true;
        }

        public String credentials() {
            return usernameInput.getText() + " " + passwordInput.getText();
        }

        @Override
        public WebDriver getDriver() {
            return null;
        }
    }

    public static class Menu extends AbstractPageObject {

        @FindBy(how = How.CSS, using = "#menu")
        @ValidatePageObjectOnInit
        private WebElement menu;

        public Menu(WebDriver driver) {
            super(driver);
        }

        public String getMenuText() {
            return menu.getText();
        }
    }

    public static class LoginPageFail extends LoginPage {
        @FindBy(how = How.CSS, using = "#passwXrd")
        @ValidatePageObjectOnInit
        private WebElement passwordInput;
    }
}