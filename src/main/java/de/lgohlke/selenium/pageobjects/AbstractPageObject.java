package de.lgohlke.selenium.pageobjects;

import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractPageObject implements PageObject {
    private final WebDriver         driver;
    private final PageObjectHelper  helper;
    private final PageFactoryHelper pageFactoryHelper;

    public AbstractPageObject(WebDriver driver) {
        this.driver = driver;
        pageFactoryHelper = new PageFactoryHelper(driver);
        helper = new PageObjectHelper(driver);
    }

    protected void waitFor(ExpectedCondition condition) {
        waitFor(condition, 15);
    }

    @SuppressWarnings("unchecked")
    protected void waitFor(ExpectedCondition condition, int seconds) {
        new WebDriverWait(driver, seconds)
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .until(condition);
    }
}
