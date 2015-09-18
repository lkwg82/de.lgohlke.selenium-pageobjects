
as maven `dependency`

```xml

<dependency>
    <groupId>de.lgohlke.selenium</groupId>
    <artifactId>pageobjects</artifactId>
    <version>0.2</version>
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