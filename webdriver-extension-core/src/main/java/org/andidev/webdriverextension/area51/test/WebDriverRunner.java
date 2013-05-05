package org.andidev.webdriverextension.area51.test;

import com.opera.core.systems.OperaDriver;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.andidev.webdriverextension.area51.utils.AnnotationUtils;
import org.andidev.webdriverextension.area51.CurrentBrowser;
import org.andidev.webdriverextension.area51.annotations.RemoteAddress;
import org.andidev.webdriverextension.area51.annotations.browsers.Android;
import org.andidev.webdriverextension.area51.annotations.browsers.Chrome;
import org.andidev.webdriverextension.area51.annotations.browsers.CustomBrowser;
import org.andidev.webdriverextension.area51.annotations.browsers.Firefox;
import org.andidev.webdriverextension.area51.annotations.browsers.HtmlUnit;
import org.andidev.webdriverextension.area51.annotations.browsers.IPad;
import org.andidev.webdriverextension.area51.annotations.browsers.IPhone;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreAndroid;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreChrome;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreCustomBrowser;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreFirefox;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreHtmlUnit;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreIPad;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreIPhone;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreInternetExplorer;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreOpera;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnorePhantomJS;
import org.andidev.webdriverextension.area51.annotations.browsers.IgnoreSafari;
import org.andidev.webdriverextension.area51.annotations.browsers.InternetExplorer;
import org.andidev.webdriverextension.area51.annotations.browsers.Opera;
import org.andidev.webdriverextension.area51.annotations.browsers.PhantomJS;
import org.andidev.webdriverextension.area51.annotations.browsers.Safari;
import org.andidev.webdriverextension.exceptions.WebDriverExtensionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.iphone.IPhoneDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

public class WebDriverRunner extends BlockJUnit4ClassRunner {

    private static class WebDriverFrameworkMethod extends FrameworkMethod {

        final private WebDriverRunner.Browser browser;

        public WebDriverFrameworkMethod(WebDriverRunner.Browser browser, FrameworkMethod method) {
            super(method.getMethod());
            this.browser = browser;
        }

        @Override
        public String getName() {
            return String.format("%s%s", super.getName(), browser.getTestDescriptionSuffix());
        }

        private WebDriverRunner.Browser getBrowser() {
            return browser;
        }
    }

    private static List<Class> browserAnnotationClasses = Arrays.asList(new Class[]{
        Android.class,
        Chrome.class,
        Firefox.class,
        HtmlUnit.class,
        IPhone.class,
        IPad.class,
        InternetExplorer.class,
        Opera.class,
        PhantomJS.class,
        Safari.class,
        CustomBrowser.class
    });
    private static List<Class> ignoreBrowserAnnotationClasses = Arrays.asList(new Class[]{
        IgnoreAndroid.class,
        IgnoreChrome.class,
        IgnoreFirefox.class,
        IgnoreHtmlUnit.class,
        IgnoreIPhone.class,
        IgnoreIPad.class,
        IgnoreInternetExplorer.class,
        IgnoreOpera.class,
        IgnorePhantomJS.class,
        IgnoreSafari.class,
        IgnoreCustomBrowser.class
    });

    public WebDriverRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> testAnnotatedMethods = getTestClass().getAnnotatedMethods(Test.class);
        List<FrameworkMethod> testMethods = new ArrayList<FrameworkMethod>();
        for (FrameworkMethod testAnnotatedMethod : testAnnotatedMethods) {
            WebDriverRunner.Browsers browsers = new WebDriverRunner.Browsers().addBrowsersFromClassAnnotations(getTestClass()).addBrowsersFromMethodAnnotations(testAnnotatedMethod);
            if (!browsers.getBrowsers().isEmpty()) {
                for (WebDriverRunner.Browser browser : browsers.getBrowsers()) {
                    testMethods.add(new WebDriverRunner.WebDriverFrameworkMethod(browser, testAnnotatedMethod));
                }
            } else {
                // Not a Selenium Grid Anotated test, treat as normal test
                testMethods.add(testAnnotatedMethod);
            }
        }
        return testMethods;
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        if (method instanceof WebDriverRunner.WebDriverFrameworkMethod) {
            WebDriverRunner.Browsers browsers = new WebDriverRunner.Browsers().addBrowsersFromClassAnnotations(getTestClass()).addBrowsersFromMethodAnnotations(method);
            WebDriverRunner.Browser browser = ((WebDriverRunner.WebDriverFrameworkMethod) method).getBrowser();
            Description description = describeChild(method);
            if (method.getAnnotation(Ignore.class) != null || browsers.isBrowserIgnored(browser)) {
                long threadId = Thread.currentThread().getId();
                notifier.fireTestIgnored(description);
            } else {
                long threadId = Thread.currentThread().getId();
                String remoteAddress = ((RemoteAddress) getTestClass().getJavaClass().getAnnotation(RemoteAddress.class)).value();
                try {
                    CurrentBrowser.setDriver(browser.createDriver());
                } catch (Exception ex) {
                    notifier.fireTestFailure(new Failure(description, ex));
                    return;
                }
                runLeaf(methodBlock(method), description, notifier);
                CurrentBrowser.getDriver().quit();
            }
        } else {
            // Not a Selenium Grid Anotated test, treat as normal test
            super.runChild(method, notifier);
        }
    }

    private class Browsers {

        TreeSet<WebDriverRunner.Browser> browsers = new TreeSet<WebDriverRunner.Browser>();
        TreeSet<WebDriverRunner.Browser> ignoreBrowsers = new TreeSet<WebDriverRunner.Browser>();

        public TreeSet<WebDriverRunner.Browser> getBrowsers() {
            return browsers;
        }

        public TreeSet<WebDriverRunner.Browser> getIgnoreBrowsers() {
            return ignoreBrowsers;
        }

        public WebDriverRunner.Browsers addBrowsersFromClassAnnotations(TestClass clazz) {
            addBrowsersFromAnnotations(clazz.getAnnotations());
            return this;
        }

        public WebDriverRunner.Browsers addBrowsersFromMethodAnnotations(FrameworkMethod method) {
            addBrowsersFromAnnotations(method.getAnnotations());
            return this;
        }

        public WebDriverRunner.Browsers addBrowsersFromAnnotations(Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (browserAnnotationClasses.contains(annotation.annotationType())) {
                    addBrowserFromAnnotation(annotation);
                } else if (ignoreBrowserAnnotationClasses.contains(annotation.annotationType())) {
                    addIgnoreBrowserFromAnnotation(annotation);
                } else if (annotation.annotationType().equals(org.andidev.webdriverextension.area51.annotations.browsers.Browsers.class)) {
                    org.andidev.webdriverextension.area51.annotations.browsers.Browsers browsersAnnotation = (org.andidev.webdriverextension.area51.annotations.browsers.Browsers) annotation;
                    addBrowsersFromAnnotations(browsersAnnotation.android());
                    addBrowsersFromAnnotations(browsersAnnotation.chrome());
                    addBrowsersFromAnnotations(browsersAnnotation.firefox());
                    addBrowsersFromAnnotations(browsersAnnotation.htmlUnit());
                    addBrowsersFromAnnotations(browsersAnnotation.iPhone());
                    addBrowsersFromAnnotations(browsersAnnotation.iPad());
                    addBrowsersFromAnnotations(browsersAnnotation.internetExplorer());
                    addBrowsersFromAnnotations(browsersAnnotation.opera());
                    addBrowsersFromAnnotations(browsersAnnotation.phantomJS());
                    addBrowsersFromAnnotations(browsersAnnotation.safari());
                    addBrowsersFromAnnotations(browsersAnnotation.customBrowser());
                } else if (annotation.annotationType().equals(org.andidev.webdriverextension.area51.annotations.browsers.IgnoreBrowsers.class)) {
                    org.andidev.webdriverextension.area51.annotations.browsers.IgnoreBrowsers ignoreBrowsersAnnotation = (org.andidev.webdriverextension.area51.annotations.browsers.IgnoreBrowsers) annotation;
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.android());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.chrome());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.firefox());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.htmlUnit());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.iPhone());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.iPad());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.internetExplorer());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.opera());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.phantomJS());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.safari());
                    addIgnoreBrowsersFromAnnotations(ignoreBrowsersAnnotation.customBrowser());
                }
            }
            return this;
        }

        public WebDriverRunner.Browsers addIgnoreBrowsersFromAnnotations(Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (browserAnnotationClasses.contains(annotation.annotationType())) {
                    addIgnoreBrowserFromAnnotation(annotation);
                }
            }
            return this;
        }

        public WebDriverRunner.Browsers addBrowserFromAnnotation(Annotation annotation) {
            browsers.add(new WebDriverRunner.Browser(annotation));
            return this;
        }

        public WebDriverRunner.Browsers addIgnoreBrowserFromAnnotation(Annotation annotation) {
            ignoreBrowsers.add(new WebDriverRunner.Browser(annotation));
            return this;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }

        public boolean isBrowserIgnored(WebDriverRunner.Browser browser) {
            if (ignoreBrowsers.contains(browser)) {
                return true;
            }
            if (ignoreBrowsersContainsBrowserWithVersionAnyAndPlatformAny(browser.getBrowserName())) {
                return true;
            }
            if (ignoreBrowsersContainsBrowserWithVersionAny(browser.getBrowserName(), browser.getPlatform())) {
                return true;
            }
            if (ignoreBrowsersContainsBrowserWithPlatformAny(browser.getBrowserName(), browser.getVersion())) {
                return true;
            }
            return false;
        }

        private boolean ignoreBrowsersContainsBrowserWithVersionAnyAndPlatformAny(String browserName) {
            for (WebDriverRunner.Browser ignoreBrowser : ignoreBrowsers) {
                if (ignoreBrowser.getBrowserName().equals(browserName)
                        && ignoreBrowser.getVersion().equals("")
                        && ignoreBrowser.getPlatform().equals(Platform.ANY)) {
                    return true;
                }
            }
            return false;
        }

        private boolean ignoreBrowsersContainsBrowserWithVersionAny(String browserName, Platform platform) {
            for (WebDriverRunner.Browser ignoreBrowser : ignoreBrowsers) {
                if (ignoreBrowser.getBrowserName().equals(browserName)
                        && ignoreBrowser.getVersion().equals("")
                        && ignoreBrowser.getPlatform().equals(platform)) {
                    return true;
                }
            }
            return false;
        }

        private boolean ignoreBrowsersContainsBrowserWithPlatformAny(String browserName, String version) {
            for (WebDriverRunner.Browser ignoreBrowser : ignoreBrowsers) {
                if (ignoreBrowser.getBrowserName().equals(browserName)
                        && ignoreBrowser.getVersion().equals(version)
                        && ignoreBrowser.getPlatform().equals(Platform.ANY)) {
                    return true;
                }
            }
            return false;
        }
    }

    public class Browser implements Comparable<WebDriverRunner.Browser> {

        private String browserName;
        private String version;
        private Platform platform;
        private String platformName;

        public Browser(String browserName, String version, Platform platform) {
            this.browserName = browserName;
            this.version = version;
            this.platform = platform;
        }

        public Browser(RemoteWebDriver driver) {
            this.browserName = driver.getCapabilities().getBrowserName();
            this.version = driver.getCapabilities().getVersion();
            this.platform = driver.getCapabilities().getPlatform();
        }

        public Browser(Annotation annotation) {

            if (annotation.annotationType().equals(Android.class)
                    || annotation.annotationType().equals(IgnoreAndroid.class)) {
                this.browserName = "android";
            } else if (annotation.annotationType().equals(Chrome.class)
                    || annotation.annotationType().equals(IgnoreChrome.class)) {
                this.browserName = "chrome";
            } else if (annotation.annotationType().equals(Firefox.class)
                    || annotation.annotationType().equals(IgnoreFirefox.class)) {
                this.browserName = "firefox";
            } else if (annotation.annotationType().equals(HtmlUnit.class)
                    || annotation.annotationType().equals(IgnoreHtmlUnit.class)) {
                this.browserName = "htmlunit";
            } else if (annotation.annotationType().equals(IPhone.class)
                    || annotation.annotationType().equals(IgnoreIPhone.class)) {
                this.browserName = "iPhone";
            } else if (annotation.annotationType().equals(IPad.class)
                    || annotation.annotationType().equals(IgnoreIPad.class)) {
                this.browserName = "iPad";
            } else if (annotation.annotationType().equals(InternetExplorer.class)
                    || annotation.annotationType().equals(IgnoreInternetExplorer.class)) {
                this.browserName = "internet explorer";
            } else if (annotation.annotationType().equals(Opera.class)
                    || annotation.annotationType().equals(IgnoreOpera.class)) {
                this.browserName = "opera";
            } else if (annotation.annotationType().equals(PhantomJS.class)
                    || annotation.annotationType().equals(IgnorePhantomJS.class)) {
                this.browserName = "phantomjs";
            } else if (annotation.annotationType().equals(Safari.class)
                    || annotation.annotationType().equals(IgnoreSafari.class)) {
                this.browserName = "safari";
            } else if (annotation.annotationType().equals(CustomBrowser.class)
                    || annotation.annotationType().equals(IgnoreCustomBrowser.class)) {
                this.browserName = (String) AnnotationUtils.getValue(annotation, "browserName");
                this.version = (String) AnnotationUtils.getValue(annotation, "version");
                this.platformName = (String) AnnotationUtils.getValue(annotation, "platform");
                return;
            }
            this.version = (String) AnnotationUtils.getValue(annotation, "version");
            this.platform = (Platform) AnnotationUtils.getValue(annotation, "platform");
        }

        public String getBrowserName() {
            return browserName;
        }

        public String getVersion() {
            return version;
        }

        public Platform getPlatform() {
            return platform;
        }

        public String getPlatformName() {
            return platform.toString();
        }



        private WebDriver createDriver() throws Exception {
            if ("android".equals(browserName)) {
                return new AndroidDriver();
            }

            if ("chrome".equals(browserName)) {
                System.setProperty("webdriver.chrome.driver", "/Users/anders/.bin/chromedriver");
                return new ChromeDriver();
            }

            if ("firefox".equals(browserName)) {
                return new FirefoxDriver();
            }

            if ("htmlunit".equals(browserName)) {
                return new HtmlUnitDriver();
            }

            if ("iPad".equals(browserName)) {
                return new IPhoneDriver();
            }

            if ("iPhone".equals(browserName)) {
                return new IPhoneDriver();
            }

            if ("internet explorer".equals(browserName)) {
                System.setProperty("webdriver.ie.driver", "/Users/anders/.bin/internetexplorerdriver");
                return new InternetExplorerDriver();
            }

            if ("opera".equals(browserName)) {
                return new OperaDriver();
            }

            if ("phantomjs".equals(browserName)) {
                return new PhantomJSDriver(null);
            }

            if ("safari".equals(browserName)) {
                return new SafariDriver();
            }

            throw new WebDriverExtensionException("Could not find any known driver for " + toString());
        }

        private Object getTestDescriptionSuffix() {
            String browserNameDescription = (browserName != null ? "[" + browserName + "]" : "[ANY]");
            String versionDescription = (version != null ? "[" + version + "]" : "[ANY]");
            String platformDescription = (platform != Platform.ANY ? "[" + platform.toString() + "]" : "[ANY]");

            return browserNameDescription + versionDescription + platformDescription;
        }

        @Override
        public int compareTo(WebDriverRunner.Browser t) {
            if (StringUtils.equals(this.getBrowserName(), t.getBrowserName())
                    && StringUtils.equals(this.getVersion(), t.getVersion())
                    && this.getPlatform().equals(t.getPlatform())) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public String toString() {
            return "Browser{" + "browserName=" + browserName + ", version=" + version + ", platform=" + platform + '}';
        }
    }
}