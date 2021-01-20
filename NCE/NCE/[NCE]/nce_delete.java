package myProject;

import static java.lang.System.exit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class nce_delete {	
	protected static String thread;
    protected static String query;
    protected static String rsQuery;

    protected static WebDriver driver;
    protected static String id;
	protected static String requestIdStr;
	protected static String commentStr;
	protected static String parallelKey;
	protected static String currentStatus;
	protected static Connection connection;
	protected static Statement stmt;
	protected static PreparedStatement update;
	protected static PreparedStatement errorUpdate;
	protected static Statement count;
	protected static ResultSet rs;
	protected static ResultSet countRs;
	protected static int rowCount;
	protected static Instant start =null;
	protected static Instant end =null;
	protected static Duration timeElapsed=null;
	protected static String warning;
	
    public static void main(String[] args) throws Exception { 
    	try {getURL();} catch (Throwable e) {System.exit(0);}  	
    	try {run(args[0]);} catch (Throwable e) {System.exit(0);}
    }
   
    public static void run(String... args) throws Throwable {
        if (args[0].length() > 0 ) { 
        	login();
        	search_menu().click();
        	request_submenu().click();
        	
        	thread = args[0].toString().trim();	
        	connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+"PPMC", "postgres", "admin");
			count = connection.createStatement();
			query="SELECT COUNT(*) FROM public.nce_delete WHERE key_status IN ('PENDING','ONGOING') AND parallel_key='"+thread+"';";
			rsQuery="SELECT * FROM public.nce_delete WHERE key_status IN ('PENDING','ONGOING') AND parallel_key='"+thread+"' ORDER BY id;";
	        countRs = count.executeQuery(query);
	        	countRs.next();	
	            rowCount = countRs.getInt(1);
	            System.out.println("RUN [" + thread + "] - TOTOAL DEMANDS FOR PROCESSING ["+rowCount+"].");
	            if (rowCount==0) {;System.exit(0);}
	            
	            for (int x = 0; x < rowCount; x++) {
	    	        try {
	    	            if (connection != null) {
	    	            System.out.println("[PARALLEL "+ thread +"]: Connection successful."); 
	    	            start = Instant.now();
	    	            System.out.println("TIME START: "+start);
	   	                stmt = connection.createStatement();
	   	                update = connection.prepareStatement("UPDATE nce_delete SET key_status = ?, status = ? WHERE parallel_key=? AND id = ?;");
	   	                rs = stmt.executeQuery(rsQuery);
	   			                while (rs.next()) {	
	   			                	requestIdStr =rs.getString("request_id".trim()); 			                   	 
				                	id=rs.getString("id".trim());
				                	commentStr=rs.getString("comment");
				                	
				                	System.out.println("[PROCESSING] "+requestIdStr);
				                	searchRequestId(requestIdStr.trim());
				                	DateFormat Date = DateFormat.getDateInstance();
				                	Calendar cals = Calendar.getInstance();
				                	String currentDate = Date.format(cals.getTime());
				                	
				                	//[MAIN]//
				                	actions(commentStr, currentDate);
				                	
				                    ongoingUpate();
									update.executeUpdate();	
									System.out.println("[PARALLEL "+ thread +"] " + requestIdStr + " processing successful.");
									Thread.sleep(2000);
							        search_menu().click();Thread.sleep(500);							        
							        request_submenu().click();Thread.sleep(500);
	   			                }
	    	               break;
	    	            } else {
	    	            	System.out.println("No connection.");
	    	            }
	    	        } catch (Exception e) {
	    	            e.printStackTrace();
	    	        }
	    	  		}
	    			 count.close();
	                 countRs.close();
	                 connection.close();
	                 driver.close();
	             	 end = Instant.now();
	             	System.out.println("TIME ENDED: "+end);
		         	timeElapsed = Duration.between(start, end);
		         	System.out.println("TIME ELAPSED: "+ timeElapsed.toMinutes()+" MINUTES");         
	    		
        }else{
            System.out.println("[ERROR] INVALID INPUT");
        }
        exit(0);
    }
    	

	//STEP 1
	public static void getURL() {
		for (int x = 0; x < 5; x++) {
			try {
				DesiredCapabilities capabilities;	    
				capabilities = DesiredCapabilities.chrome();
				ChromeOptions options = new ChromeOptions(); 
			    	    //options.addArguments("--headless");   
			    	    options.addArguments("--disable-extensions");   
			    	    options.addArguments("--disable-gpu");   
			    	    options.addArguments("--no-sandbox");   
			    	    options.addArguments("--window-size=1600,900");   
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
				options.merge(capabilities);
				driver = new ChromeDriver(options);
		    	driver.get("https://ppmcpro.itcs.houston.dxccorp.net/");
		    	driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		    	driver.manage().window().maximize();
				break;
			} catch (Exception e) {
			}	
		}
	}

	
	//STEP 2
	public static void login() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@name,'USER')]")));
			WebElement username = driver.findElement(By.name("USER"));
	    	WebElement password = driver.findElement(By.name("PASSWORD"));
	    	WebElement loginBtn = driver.findElement(By.id("loginbtn"));
	    	username.sendKeys("jdionisio4");
	    	password.sendKeys("Jcsd!1206");
	    	loginBtn.click();
			break;
		} catch (Exception e) {
			loginWait();
		}
		}
	}

	//STEP 3
    public static WebElement search_menu() {
		for (int x = 0; x < 30; x++) {
		try {
			Thread.sleep(500);
			WebDriverWait wait = new WebDriverWait(driver, 30);
			By elemPath = By.xpath("//a[contains(@href,'#MENU_SEARCH')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//a[contains(@href,'#MENU_SEARCH')]"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] SEARCH MENU");
		}
	}
		return null;
	}
  
    //STEP 4
	public static WebElement request_submenu() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 30);
			By elemPath = By.xpath("//a[contains(@class,'yuimenuitemlabel')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//a[contains(text(),'Requests')]"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] REQUEST SUB-MENU");
		}
	  }
		return null;
	}
	
	//STEP 5
	public static WebElement request_id() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			By elemPath = By.xpath("//input[contains(@name,'REQUEST_ID')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//input[contains(@name,'REQUEST_ID')]"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] REQUEST ID");
			search_menu().click();
        	request_submenu().click();
		}
		}
		return null;
	}
	//STEP 5 - SUB
	private static void searchRequestId(String text) {
		for (int x = 0; x < 20; x++) {
			try {
				request_id().clear();
				request_id().sendKeys(text);
				request_id().sendKeys(Keys.ENTER);
				break;
			} catch (Exception e) {
				driver.navigate().refresh();
				System.err.println("[WAITING] REQUEST ID SEND");
			}
		}
	}
	
	//STEP 6
	private static void actions(String commentVal, String dateVal) {
		for (int x = 0; x < 10; x++) {
		try {
//			driver.switchTo().frame("pfmIframe");
			cancelButton().click();
//			clearForecastDate().clear();clearForecastDate().sendKeys(dateVal);
			Thread.sleep(1000);
			cancelNote().sendKeys(commentVal);
			cancelFinal().click();
//			driver.switchTo().defaultContent();
			break;
		} catch (Exception e) {
			System.out.println("[WAITING] ACTION REQUIRED");
			statusElemWait();
		}
	 }	
	}
	
	//STEP 6 - SUB
	public static WebElement cancelButton() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver,30);
			By elemPath = By.xpath("//*[contains(@id,'DB1_0')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//*[contains(@id,'DB1_0')]/div"));
			return element;
		} catch (Exception e) {
			System.out.println("[WAITING] CANCEL BUTTON");
		}
		}
		return null;
	}
	

	//STEP 7
	private static void ongoingUpate() {
		for (int x = 0; x < 20; x++) {
		try {
			currentStatus = statusWait(); 
			if (currentStatus.trim().contains("Closed")){
				 update.setString(1, "COMPLETED");
			}else {
				 update.setString(1, "DONE");	
			}	
	         update.setString(2, currentStatus);
	         update.setString(3, thread);
	         update.setString(4, id);
	         break;
		} catch (Exception e) {
			System.out.println("[RETRY] UPDATE FAILED");
		}
		}		
	}
	
	private static WebElement clearForecastDate() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			By elemPath = By.id("REQ.P.CLEAR_FROM_DATE");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.id("REQ.P.CLEAR_FROM_DATE"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] ORIGINAL POSITION FIELD");
		}
		}
		return null;
	}
	
	private static WebElement cancelNote() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("REQ.LOOKAHEAD.NEW_NOTE");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.id("REQ.LOOKAHEAD.NEW_NOTE"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] CANCEL NOTE FIELD");
		}
		}
		return null;
	}
	
	public static WebElement cancelFinal() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("btnContinue");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.id("btnContinue"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] RELEASE FOR APPROVAL BUTTON");
		}
		}
		return null;
	}
	
	
	
	private static String statusWait() {
		for (int x = 0; x < 20; x++) {
			try {
				WebDriverWait wait = new WebDriverWait(driver, 30);
				By elemPath = By.xpath("//*[@id=\"requestStatus\"]/span/a/span"); 
				WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
				String elemText = wait.until(ExpectedConditions.elementToBeClickable(elem)).getText().toString();
				return elemText;
				} catch (Exception e) {
					driver.navigate().refresh();
					System.out.println("[WAITING] STATUS");
			}
		}
		return null;
	}
	
	public static void statusElemWait() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@id,'requestStatus')]")));
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] STATUS ELEMENT");
		}
		}
	}
	
	public static void  loginWait() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@name,'USER')]")));
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] LOGIN FIELDS");
		}
		}
	}
	
	public static void processingWait() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 30);
			By loader = By.xpath("//img[contains(@src,'Loading.gif')]");
			By waiting = By.xpath("//*[@id='page-min-width-div']/div[5]/div/form/div[3]/span[1]/img");
			wait.until(ExpectedConditions.invisibilityOfElementLocated(loader));
			wait.until(ExpectedConditions.invisibilityOfElementLocated(waiting));
			ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");}
			};
			wait.until(condition);
		} catch (Exception e) {
		}
	   }
	}
}
