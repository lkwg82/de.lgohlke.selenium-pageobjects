package de.lgohlke.selenium.pageobjects;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Slf4j
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
        try {
            new WebDriverWait(driver, seconds)
                    .pollingEvery(500, TimeUnit.MILLISECONDS)
                    .until(condition);
        } catch (Exception e) {
            propagateToEventlisteners(driver, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void propagateToEventlisteners(WebDriver driver, Exception exception){
        if (driver instanceof EventFiringWebDriver) {
            try {
                Field field = EventFiringWebDriver.class.getDeclaredField("eventListeners");
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                try {
                    List<WebDriverEventListener> eventListeners = (List<WebDriverEventListener>) field.get(driver);
                    eventListeners.forEach(listener -> listener.onException(exception, driver));
                } finally {
                    field.setAccessible(accessible);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }else{
            throw new IllegalStateException(exception);
        }
    }
}
