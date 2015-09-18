package de.lgohlke.selenium.pageobjects;

import org.openqa.selenium.WebDriver;

public interface PageObject {
    WebDriver getDriver();

    /**
     * can be overriden to do some before init handling
     */
    default void beforeInit() {
    }
}
