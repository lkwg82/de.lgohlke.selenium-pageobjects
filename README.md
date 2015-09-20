[![travisci](https://travis-ci.org/lkwg82/de.lgohlke.selenium-pageobjects.svg)](https://travis-ci.org/lkwg82/de.lgohlke.selenium-pageobjects)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.lgohlke.selenium/pageobjects/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3Ade.lgohlke.selenium)

Note: it is similiar to https://github.com/webdriverextensions/webdriverextensions, but offers a lifecycle for all webcomponents

as maven `dependency`

```xml

<dependency>
    <groupId>de.lgohlke.selenium</groupId>
    <artifactId>pageobjects</artifactId>
    <version>0.3</version>
</dependency>

```

```java
    public class LoginPage implements PageObject {
        @FindBy(how = How.CSS, using = "#login")
        @ValidatePageObjectOnInit
        private WebElement usernameInput;

        @FindBy(how = How.CSS, using = "#password")
        @ValidatePageObjectOnInit
        private WebElement passwordInput;
        
        @ValidatePageObjectOnInit            
        private Menu menu;
    
        public void beforeInit() {
            /* do something */
        }
        
        public void logout(){
            menu.logout(); 
        }
    }


     public class Menu extends AbstractPageObject {
    
        @FindBy(how = How.CSS, using = "#menu")
        @ValidatePageObjectOnInit
        private WebElement menu;

        public Menu(WebDriver driver) {
            super(driver);
        }

        public void logout() {
            return menu.click();
        }
    }
    
    ...
    @Test
    public void should_init_and_logout(){
          
        driver.get("http://somewhere");

        LoginPage page = pageFactoryHelper.initElements(LoginPage.class);
        page.logout();
        ...
    }
    
```
