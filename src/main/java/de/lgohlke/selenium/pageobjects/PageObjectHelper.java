package de.lgohlke.selenium.pageobjects;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@RequiredArgsConstructor
public class PageObjectHelper {
    private final WebDriver driver;

    public boolean isPresent(WebElement element) {
        try {
            element.getSize();
            return true;
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
