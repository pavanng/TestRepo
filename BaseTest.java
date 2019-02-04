package core;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * Created by daniel on 1/22/15.
 */
public class BaseTest
{


    public boolean isConfluence = System.getenv("app").equalsIgnoreCase("confluence");
    public boolean isJIRA = System.getenv("app").equalsIgnoreCase("jira");
    public boolean isJIRAOld = (System.getenv("oldjira")!=null && System.getenv("oldjira").equalsIgnoreCase("true"));
    public boolean isStash = System.getenv("app").equalsIgnoreCase("stash");



    public String sessionId;

    public String username = System.getenv("sauceUsername");
    public String key = System.getenv("sauceKey");
    public String baseurl = "http://"+System.getenv("baseUrl");



    public WebDriver driver = null;
    public boolean isLocalDriver = false;
    public WebDriver getDriver(String name) throws Exception
    {
        if( this.driver ==null)
        {

            if(username!=null) {

                DesiredCapabilities caps = DesiredCapabilities.firefox();
                caps.setPlatform(Platform.WINDOWS);
                caps.setCapability("name", name);
                RemoteWebDriver remoteWebDriver = new RemoteWebDriver(
                        new URL("http://" + username + ":" + key + "@ondemand.saucelabs.com:80/wd/hub"),
                        caps);
                remoteWebDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
                remoteWebDriver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

                sessionId = remoteWebDriver.getSessionId().toString();
                this.driver = remoteWebDriver;
                this.isLocalDriver = false;
            }
            else
            {
                this.driver = new FirefoxDriver();
                this.isLocalDriver = true;
                this.driver.manage().window().maximize();
                this.driver.manage().window().setSize( new Dimension(1200, 4000));
                this.driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                this.driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);


                this.driver.get(baseurl);

                this.driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                
            }

        }

        return this.driver;
    }


    public void removeDriver()
    {

        if(this.driver!=null)
        {
            this.driver.quit();
        }
        this.driver = null;

    }


    public void waitForPageLoaded(WebDriver driver) {

        ExpectedCondition<Boolean> expectation = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };

        Wait<WebDriver> wait = new WebDriverWait(driver,30);
        try {
            wait.until(expectation);
        } catch(Throwable error) {
            error.printStackTrace();
        }
    }

    public WebElement waitForElement(   WebDriver driver,  final By locator)
    {
        return this.waitForElement(driver, locator, 5);
    }



    public WebElement waitForElement(   WebDriver driver,  final By locator, int timeout)
    {

        WebDriverWait wait = new WebDriverWait( driver, timeout);
        try
        {

            wait.pollingEvery(1, TimeUnit.SECONDS).withTimeout(timeout, TimeUnit.SECONDS).ignoring(NoSuchElementException.class).until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
        catch(TimeoutException e)
        {
            e.printStackTrace();
        }
        this.waitABit();

        return driver.findElement(locator);
    }

    public void takeScreenshot( WebDriver driver, String name)
    {
        File screenshotDir = new File("screenshots");
        screenshotDir.mkdirs();
        File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        File finalFile = new File( screenshotDir,name+".png");
        System.err.println( finalFile.getAbsolutePath());
        try
        {
            FileUtils.moveFile(tempFile, finalFile);
        }
        catch(Exception e) {
            e.printStackTrace();
            System.err.println("Unable to move file");
        }

    }

    public WebElement waitForElementSpecial(   WebDriver driver,  final By locator)
    {
        File screenshotDir = new File("screenshots");
        screenshotDir.mkdir();

        int timeout = 30;
        WebDriverWait wait = new WebDriverWait( driver, timeout);
        try
        {

            wait.pollingEvery(1, TimeUnit.SECONDS).withTimeout(30, TimeUnit.SECONDS).ignoring(NoSuchElementException.class).until(ExpectedConditions.elementToBeClickable(locator));
        }
        catch(TimeoutException e)
        {
            e.printStackTrace();
        }
        this.waitABit();

        System.err.println("Taking screenshot and moving it to "+ screenshotDir.getAbsolutePath());

        File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try
        {
            FileUtils.moveFileToDirectory(tempFile, screenshotDir, true);
        }
        catch(Exception e) {
            e.printStackTrace();
            System.err.println("Unable to move file");
        }
        return driver.findElement(locator);
    }



    public WebElement waitForElementClickable(   WebDriver driver,  final By locator)
    {
        int timeout = 30;
        WebDriverWait wait = new WebDriverWait( driver, timeout);
        try
        {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
        catch(TimeoutException e)
        {
            e.printStackTrace();
        }
        this.waitABit();
        return driver.findElement(locator);
    }

    public boolean waitForTextOnPage(   WebDriver driver, String text)
    {
        int timeout = 15;
        this.waitABit();
        WebDriverWait wait = new WebDriverWait( driver, timeout);
        try
        {
            wait.until(ExpectedConditions.textToBePresentInElement(By.tagName("html"), text) );
            return true;
        }
        catch(TimeoutException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void waitABit()
    {
        try { Thread.sleep(1000l); } catch (Exception e) { throw new RuntimeException(e); }
    }
    public void waitABit( int secs)

    {
        try { Thread.sleep(secs * 1000l); } catch (Exception e) { throw new RuntimeException(e); }
    }


    public void triggerReindex( )
    {


        byte[] credentials = Base64.encodeBase64(("admin:admin").getBytes());



        String host = "";
        int port = 8080;
        String contextPath = "";

        System.err.println(baseurl);
        try
        {
            URL url = new URL(baseurl);
            host = url.getHost();
            port = url.getPort();
            contextPath = url.getPath();
        }

        catch(MalformedURLException urlException)
        {
            Assert.fail( urlException.getMessage());
        }


        try {
            HttpHost httpHost = new HttpHost(host, port);
            HttpClient httpClient = new DefaultHttpClient();


            System.err.println( "Posting to /rest/api/2/reindex");
            HttpPost request = new HttpPost(contextPath+"/rest/api/2/reindex?type=BACKGROUND_PREFFERED");

            request.addHeader("Authorization", "Basic " + new String(credentials));



            HttpResponse response = httpClient.execute(httpHost, request);
            System.err.println("Indexing triggered: "+response.getStatusLine().getStatusCode());


            EntityUtils.consume(response.getEntity());




        }
        catch(Exception e)
        {
            System.err.println( e.getMessage());
            e.printStackTrace();
        }



    }

    public void handleExtraSecurity(WebDriver wd)
    {
        this.handleExtraSecurity(wd, "admin");
    }

    protected void handleExtraSecurity(WebDriver wd, String password)
    {
        if(this.isConfluence)
        {
            try {
                this.waitForElement(wd, By.id("password")).click();
                wd.findElement(By.id("password")).clear();
                wd.findElement(By.id("password")).sendKeys(password);
                wd.findElement(By.id("authenticateButton")).click();
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        else if(this.isJIRA) {
            try {
                this.waitForElement(wd, By.id("login-form-authenticatePassword")).click();
                wd.findElement(By.id("login-form-authenticatePassword")).clear();
                wd.findElement(By.id("login-form-authenticatePassword")).sendKeys(password);
                wd.findElement(By.id("login-form-submit")).click();
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
    }



    public void logInUser(WebDriver wd, String name, String password, String destination)
    {
        if(isConfluence) {
            try {
                System.err.println("Logging into " + baseurl + "/login.action?os_destination=" + URLEncoder.encode(destination, "UTF-8"));
                wd.get(baseurl + "/login.action?os_destination=" + URLEncoder.encode(destination, "UTF-8"));
                System.err.println("Done");
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            wd.findElement(By.id("os_username")).click();
            wd.findElement(By.id("os_username")).clear();
            wd.findElement(By.id("os_username")).sendKeys(name);
            wd.findElement(By.id("os_password")).click();
            wd.findElement(By.id("os_password")).clear();
            wd.findElement(By.id("os_password")).sendKeys(password);
            wd.findElement(By.id("loginButton")).click();
        }
        if(isJIRA) {
            try {
                wd.get(baseurl + "/login.jsp?os_destination=" + URLEncoder.encode(destination, "UTF-8"));
            }
            catch(Exception e)
            {
                System.err.println( e.getMessage());
                e.printStackTrace();
            }
            this.takeScreenshot(wd, "login");
            this.waitForElement( wd, By.id("login-form-username")).click();
            wd.findElement(By.id("login-form-username")).clear();
            wd.findElement(By.id("login-form-username")).sendKeys(name);
            wd.findElement(By.id("login-form-password")).click();
            wd.findElement(By.id("login-form-password")).clear();
            wd.findElement(By.id("login-form-password")).sendKeys(password);
            wd.findElement(By.id("login-form-submit")).click();
        }

        if(this.isStash)
        {
            try {
                System.err.println("Logging into "+ baseurl + "/login?next=" + URLEncoder.encode(destination, "UTF-8"));
                wd.get(baseurl + "/login?next=" + URLEncoder.encode(destination, "UTF-8"));
                System.err.println("Done");
            }
            catch(Exception e)
            {
                System.err.println( e.getMessage());
                e.printStackTrace();
            }
            wd.findElement(By.id("j_username")).click();
            wd.findElement(By.id("j_username")).clear();
            wd.findElement(By.id("j_username")).sendKeys(name);
            wd.findElement(By.id("j_password")).click();
            wd.findElement(By.id("j_password")).clear();
            wd.findElement(By.id("j_password")).sendKeys(password);
            wd.findElement(By.id("submit")).click();
        }

    }

    public void logOut( WebDriver wd)
    {

        if(isConfluence)
        {
            wd.get(baseurl + "/logout.action");
        }
        else if(isJIRA)
        {

            wd.get(baseurl+"/logout");


            if( !wd.findElements(By.id("confirm-logout-submit")).isEmpty())
            {
                wd.findElement(By.id("confirm-logout-submit")).click();
            }
        }
        else if(this.isStash)
        {

            wd.get(baseurl+"/j_stash_security_logout");
        }
        wd.manage().deleteAllCookies();

    }


    public void waitOnFormElement( WebDriver wd, String id)
    {

        String attr = this.waitForElement(wd,By.id(id)).getAttribute("disabled");
        while(attr!=null && (attr.equals("true") || (attr.equals("disabled")) ) )
        {
            System.err.println( attr);

            this.waitABit();
            attr = this.waitForElement(wd,By.id(id)).getAttribute("disabled");
        }



    }


    public void sendEscape( WebDriver wd)
    {
        wd.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }


    public class ScreenshotTestRule implements MethodRule {

        public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        statement.evaluate();
                    } catch (Throwable t) {
                        captureScreenshot(frameworkMethod.getName());
                        throw t; // rethrow to allow the failure to be reported to JUnit
                    }
                }

                public void captureScreenshot(String fileName) {
                    try {
                        System.err.println("Taking screenshot: "+fileName);
                        File dir =new File("target/surefire-reports");
                        dir.mkdirs(); // Insure directory is there


                        File tempFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
                        FileUtils.copyFile(tempFile, new File(dir, fileName + ".png"));
                    } catch (Exception e) {
                        // No need to crash the tests if the screenshot fails
                    }
                }
            };
        }
    }

    public void handleOverlay(WebDriver wd)
    {
        this.waitABit();this.waitABit();
        try
        {
            if( wd.findElement(By.id("dont-show-whats-new"))!=null)
            {
                if(wd.findElement(By.id("dont-show-whats-new")).isDisplayed())
                {
                    wd.findElement(By.id("dont-show-whats-new")).click();
                }
                wd.findElement(By.xpath("//div[@id='whats-new-dialog']//button[.='Close']")).click();
            }
        }
        catch(Exception e)
        {

        }
    }





    public void setTextField(WebDriver wd, By by, String contents)
    {
        if(contents!=null)
        {
            WebElement field = wd.findElement( by);
            field.click();
            field.clear();
            field.sendKeys( contents);
        }
    }

    public void checkCheckbox(WebDriver wd, By by, boolean value)
    {
        WebElement field = wd.findElement(by);
        if( field.isEnabled())
        {
            if (value && !field.isSelected()) {
                field.click();
            }
            if (!value && field.isSelected()) {
                field.click();
            }
        }

    }

    public void selectFromSelect( WebDriver wd, By by, String value)
    {

        Select select = new Select( wd.findElement( by));
        select.selectByVisibleText(value);

        this.takeScreenshot(wd, "select-"+ value);
    }



}
