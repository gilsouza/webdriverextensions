package com.github.webdriverextensions.internal.junitrunner;

import com.github.webdriverextensions.internal.utils.FileUtils;
import com.github.webdriverextensions.internal.utils.OsUtils;
import com.github.webdriverextensions.internal.utils.PropertyUtils;
import com.github.webdriverextensions.junitrunner.annotations.DriverPaths;

public class DriverPathLoader {

    private static final String CHROME_DRIVER_PROPERTY_NAME = "webdriver.chrome.driver";
    private static final String IE_DRIVER_PROPERTY_NAME = "webdriver.ie.driver";
    private static final String INTERNET_EXPLORER_DRIVER_PROPERTY_NAME = "webdriver.internetexplorer.driver"; // Alternative property name that follows naming convention
    private static final String IE_DRIVER_USE64BIT_PROPERTY_NAME = "webdriverextensions.ie.driver.use64Bit";
    private static final String INTERNET_EXPLORER_DRIVER_USE64BIT_PROPERTY_NAME = "webdriverextensions.internetexplorer.driver.use64Bit";

    public static void loadDriverPaths(DriverPaths driverPaths) {
        loadChromeDriverPath(driverPaths != null ? driverPaths.chrome() : null);
        loadInternetExplorerDriverPath(driverPaths != null ? driverPaths.internetExplorer() : null);
        makeSureDriversAreExecutable();
    }

    private static void loadChromeDriverPath(String path) {
        PropertyUtils.setPropertyIfNotExists(CHROME_DRIVER_PROPERTY_NAME, path);
    }

    private static void loadInternetExplorerDriverPath(String path) {
        PropertyUtils.setPropertyIfNotExists(IE_DRIVER_PROPERTY_NAME, System.getProperty(INTERNET_EXPLORER_DRIVER_PROPERTY_NAME)); // Alternative property name that follows naming convention
        PropertyUtils.setPropertyIfNotExists(IE_DRIVER_PROPERTY_NAME, path);
    }

    private static void makeSureDriversAreExecutable() {
        FileUtils.makeExecutable(System.getProperty(CHROME_DRIVER_PROPERTY_NAME));
        FileUtils.makeExecutable(System.getProperty(IE_DRIVER_PROPERTY_NAME));
    }
}
