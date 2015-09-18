package de.lgohlke.selenium.pageobjects;

public interface Page {
    /**
     * will be used to call location of page before continuing initialisation
     * will not be call, if the location returns an empty string
     */
    String getLocation();
}
