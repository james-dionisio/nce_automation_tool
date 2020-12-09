/*Author: Reynald Perez
 * Co-author:James Dionisio
 * Last Update: Nov 16,2020
 * CREATE NCE AUTOMATION
 * */
package myProject;

import static java.lang.System.exit;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.Alert;
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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


public class nce_create {	
	protected static String thread;
    protected static String query;
    protected static String rsQuery;
    protected static List<String> dataList = new ArrayList<String>();	
    protected static int n;
    protected static WebDriver driver;
    protected static String id;
	protected static String projIDStr;
	protected static String fteDateStr;
	protected static String reasonStr;
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
	protected static Instant startRec =null;
	protected static Instant endRec =null;
	protected static Duration timeElapsed=null;
	protected static Duration timeElapsedRec=null;
	protected static String warning;
	protected static String error;
	protected static String reqID;
	
    public static void main(String[] args) throws Exception { 
    	try {getURL();} catch (Throwable e) {System.exit(0);}  	
    	try {run(args[0]);} catch (Throwable e) {System.exit(0);}
    }
   
    @SuppressWarnings("null")
	public static void run(String... args) throws Throwable {
        if (args[0].length() > 0 ) { 
        	login();
        	search_menu().click();
        	request_submenu().click();
        	submenu_wfm().click();
        	
        	thread = args[0].toString().trim();	
        	connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+"PPMC", "postgres", "admin");
			count = connection.createStatement();
			query="SELECT COUNT(*) FROM public.nce_create WHERE key_status IN ('PENDING','ONGOING') AND parallel_key='"+thread+"';";
			rsQuery="SELECT * FROM public.nce_create WHERE key_status IN ('PENDING','ONGOING') AND parallel_key='"+thread+"' ORDER BY id;";
	        countRs = count.executeQuery(query);
	        	countRs.next();	
	            rowCount = countRs.getInt(1);
	            start = Instant.now();
	            System.out.println("");
	            System.out.println("[THREAD "+ thread +"]: PROCESSING...");
	            System.out.println("TIME START: "+start);
	            System.out.println("TOTAL 'CREATE' DEMAND(S) ["+rowCount+"]");
	            if (rowCount==0) {driver.quit();System.exit(0);}
	            for (int x = 0; x < rowCount; x++) {
	    	        try {
	    	            if (connection != null) {
	    	             
	    	            startRec= Instant.now();
	   	                stmt = connection.createStatement();
	   	             update = connection.prepareStatement("UPDATE nce_create SET key_status = ?, request_id = ?, status = ?, duration = ? WHERE parallel_key=? AND id = ?;");
	   	                rs = stmt.executeQuery(rsQuery);	
	   			                while (rs.next()) {	
	   			                	System.out.println("");
	   			                	error="";
	   			                	projIDStr=rs.getString("project_id".trim()); 			                   	 
				                	id=rs.getString("id".trim());
				                	fteDateStr=rs.getString("fte".trim());
				                	System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> PROCESSING");
				                	
				                	dataList.clear();
				                	for (int count=1; count <= 47;count++) {
				                		dataList.add(rs.getString(count));
				                	}
				                	
				                	//[MAIN]//
				                	populate_projectDetails(projIDStr, fteDateStr, dataList);
				                	
				                	if (!error.isEmpty()) {
				                		   ongoingUpate();
						                	update.executeUpdate();
						                	System.out.println();
										System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [SKIPPED]");
				                	}else {
				                		submit().click();
				                		error();

										alertHandler();
				                		
				                		//PROCEED TO CLICK REQUEST ID AND APPROVALS
				                		if(error.isEmpty()) {
						                	reqID().click();
						                	completePLM().click();
						    					ongoingUpate();
						    					update.executeUpdate();	
						                	releaseForAppvl().click();
						                		ongoingUpate();
						                		update.executeUpdate();
						                	statusElemWait();currentStatus = statusWait();
						                	//CHECK IF PENDING ADL APROVAL CLICK APPROVE BTN
						                	  if (currentStatus.trim().contains("Pending ADL Approval")||currentStatus.trim().contains("Pending AE Approval")){
						                		  System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >>  APPROVAL RELEASED");
						                		  System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> " + currentStatus); 
						                		  
						                		  if(approveBtn()) {
							                		  approveADL().click();
						                		  } else {
						                			  error="[Error] Approval Button Not Activated"; 
						                		  }
						                	  }
						                	  statusElemWait();currentStatus = statusWait();
							                  Thread.sleep(100);
							                  
						                	  if (currentStatus.trim().contains("Pending Dmd Planner Approval")) {
						                		  System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> " + currentStatus); 
						                		  if(approveBtnDmdPlanner()) {
						                			  approveADLDmdPlanner().click();
						                		  } else {
						                			  error="[Error] Approval Button Not Activated"; 
						                		  }
						                	  }
						                	  
						                	  statusElemWait();currentStatus = statusWait();
							                  Thread.sleep(100);
							                  
						                	  //CHECK STATUS IF MOVE TO SP,THEN CLICK MOVETO SP BTN
						                	  if (currentStatus.trim().contains("PLM Approved")) {
						                		  System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> " + currentStatus);
						                		  moveToSp().click();
						                	  }
						                	  
						                	  statusElemWait();currentStatus = statusWait();
							                  Thread.sleep(100);
							                  
							                  if (currentStatus.trim().contains("Position Created in SP")) {
												  error="DONE"; 
											  }

							                  
							                  ongoingUpateWithReqId();
							                	update.executeUpdate();
							                	System.out.println();
												System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [SUCCESSFUL]");
				                		} else {
				                			ongoingUpate();
						                	update.executeUpdate();
						                	System.out.println();
						                	System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [SKIPPED]");
				                		}
				                		

				                	}
				                	
				                	Thread.sleep(1000);
				                	search_menu().click();
				                	Thread.sleep(500);
				                	request_submenu().click();
				                	Thread.sleep(500);
				                	submenu_wfm().click();
				                	Thread.sleep(500);
				                	alertHandlermenu();
									
	   			                }
	    	               break;
	    	            } else {
	    	            	System.out.println("No connection.");
	    	            }
	    	        } catch (Exception e) {
	    	        	System.out.println("You're here");
	    	        	ongoingUpate();
	                	update.executeUpdate();
	                	System.out.println();
	                	System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [SKIPPED]");
	    	        }
	    	  		}
	    			 count.close();
	                 countRs.close();
	                 connection.close();
	                 driver.close();
	             	 end = Instant.now();
	             	System.out.println("");
	             	System.out.println("TIME ENDED: "+end);
		         	timeElapsed = Duration.between(start, end);
		         	System.out.println("TIME ELAPSED: "+ timeElapsed.toMinutes()+" MINUTES");         
	    		
        }else{
            System.out.println("[ERROR] INVALID INPUT");
        }
        exit(0);
    }
    
	public static WebElement approveADLDmdPlanner() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			By elemPath = By.xpath("//*[@id=\"DB0_0\"]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//*[@id=\"DB0_0\"]"));
			System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [Approved DMD PLANNER]");
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] Approval BUTTON");
		}
		}
		return null;
	}
    
	public static boolean approveBtnDmdPlanner() {
		for (int x = 0; x < 20; x++) {
		try {
			Thread.sleep(100);
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("DB0_0");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			System.out.println("Approval Button Activated: "+ elem.isDisplayed());
			if (elem.isDisplayed()) {
				return true;
			}else{

				error="Approval button not active";
				return false;
			}
		} catch (Exception e) {
		}
		}
		return false;
	}
    
	public static boolean error() {
		for (int x = 0; x < 20; x++) {
		try {
			
			if(driver.findElements(By.id("errorInformation")).size()>0)		 
			{
//			   System.out.println("The element present");
				Thread.sleep(100);
				WebDriverWait wait = new WebDriverWait(driver, 5);
				String HeaderTxt = driver.findElement(By.xpath("//*[@id=\"emptyFieldsLink\"]")).getText();
				By elemPath = By.id("errorInformation");
				WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
				
				if (elem.isDisplayed()) {
					System.out.println("[Error]"+HeaderTxt);
					error="[Error]"+HeaderTxt;
					return true;
				}else{
					error="";
					return false;
				}
			}else
			{
//			   System.out.println("this element is missing");
			   error="";
			   return false;
			}

		} catch (Exception e) {
			error="";
			return false;
		}
		}
		return false;
	}
    
    public static boolean approveBtn() {
		for (int x = 0; x < 5; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("DB1_0");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			System.out.println("Approval Button Activated: "+ elem.isDisplayed());
			if (elem.isDisplayed()) {
				return true;
			}else{

				error="Approval button not active";
				return false;
			}
		} catch (Exception e) {
		}
		}
		return false;
	}
    
    public static WebElement approveADL() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			By elemPath = By.xpath("//*[@id=\"DB1_0\"]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//*[@id=\"DB1_0\"]"));
			System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [Approved ADL]");
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] Approval BUTTON");
		}
		}
		return null;
	}
    
    public static WebElement moveToSp() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			By elemPath = By.xpath("//*[@id=\"DB0_0\"]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//*[@id=\"DB0_0\"]"));
			System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [Move to SP]");
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] Move yo SP BUTTON");
		}
		}
		return null;
	}
   
//	populate_projectDetails FUNCTION POPULATES PPMC FIELDS WITH TEMPLATES DATA
	public static void populate_projectDetails(String projIDVal, String fteDateVal, List<String> dataArryVal) throws Throwable {
		for (int x = 0; x < 10; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			projID().clear();
			projID().sendKeys(projIDVal);
			projID().sendKeys(Keys.TAB);projID().sendKeys(Keys.TAB);	
			invalidataHandler(projIDVal);
			if (!error.isEmpty()) {break;}
					
			forecastEdit().click();
			
			StringTokenizer tokenizedData = new StringTokenizer(fteDateVal, ",");
			int forecastLine = tokenizedData.countTokens(); 
			String array_dataForecast[] = new String[forecastLine];
			
			for (int line = 0; line < forecastLine; line++) {
				array_dataForecast[line] = tokenizedData.nextToken(); 
				String ftevalues = array_dataForecast[line];
				StringTokenizer dataPerLine = new StringTokenizer(ftevalues, "#");
				int dataLine = dataPerLine.countTokens();
				String array_dataLine[] = new String[dataLine];
				
				By addButton = By.id("BT_ADD_ROW_P_1");
				WebElement fteaddPath = wait.until(ExpectedConditions.presenceOfElementLocated(addButton));
				wait.until(ExpectedConditions.elementToBeClickable(fteaddPath)).click();
				By enddateCol = By.xpath("//span[contains(text(), 'FTE End Date')]");
				WebElement enddateElem = wait.until(ExpectedConditions.presenceOfElementLocated(enddateCol));
				wait.until(ExpectedConditions.elementToBeClickable(enddateElem)).click();

				
				for (int dataCount = 0; dataCount < dataLine; dataCount++) { 
					array_dataLine[dataCount] = dataPerLine.nextToken();
					By fieldPath = By.id("49025_COL_"+dataCount);
					WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(fieldPath));
					wait.until(ExpectedConditions.elementToBeClickable(field)).clear();
					field.sendKeys(array_dataLine[dataCount]);
					field.sendKeys(Keys.TAB);
					Thread.sleep(1000);
					alertHandler();
					if (!error.isEmpty()) {
						System.out.println("[ERROR]:"+error);
						break;
					}
				}
			}
			Thread.sleep(100);
			
			
			
			//Populate create fields
			for (int ctr = 1; ctr <= 33; ctr++) {
				 String ctrStr=Integer.toString(ctr);
				 System.out.print("."); 
		         System.out.flush();
				 try (InputStream input = new FileInputStream("src/main/resources/properties/elements.properties")) {
			            Properties prop = new Properties();
			            prop.load(input);
			            System.out.println(ctr+"|"+prop.getProperty(ctrStr)+"|"+dataList.get(ctr+12));
			            
						if (ctr==11){
							WebElement element = driver.findElement(By.id("REQD.P.WFM_LOCTAION_CONTRACTUALLY_Y"));
							 element.click();
							 System.out.println(element.isSelected());
							 if(!element.isSelected()) {
								 element.click();
							 }
							
						}

				            By fieldPath = By.id(prop.getProperty(ctrStr));
							wait.until(ExpectedConditions.presenceOfElementLocated(fieldPath));
							wait.until(ExpectedConditions.elementToBeClickable(fieldPath));
							WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(fieldPath));
							Thread.sleep(1000);
							if (ctr==8) {
								field.clear();
							}
			
							field.sendKeys(dataList.get(ctr+12).trim());

							field.sendKeys(Keys.TAB);
						//Alert handler Latest 12/5/2020
						alertHandler();
						if (!error.isEmpty()) {
							System.out.println("[ERROR]:"+error);
							break;
						}
						Thread.sleep(1000);
						invalidataHandler(dataList.get(ctr+12));
						Thread.sleep(1000);
						if (!error.isEmpty()) {
							System.out.println("[ERROR]:"+error);
							break;
						}
			        } catch (Exception e) {
			        	
			        }
			          
			}
				
			break;
			
		} catch (Exception e) {
		}
		
	  }
		
	}

	//STEP 1
	public static void getURL() {
		for (int x = 0; x < 5; x++) {
			try {
				System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
				DesiredCapabilities capabilities;	    
				capabilities = DesiredCapabilities.chrome();
				ChromeOptions options = new ChromeOptions(); 
						options.addArguments("--headless");	    
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
				driver.navigate().refresh();
				System.out.println("[ERROR] INTERUPTION OCCURED");
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
	    	password.sendKeys("Jcsd(1206");
	    	loginBtn.click();
			break;
		} catch (Exception e) {
			loginWait();
		}
		}
	}

	//STEP 3
    public static WebElement search_menu() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.xpath("//a[contains(@href,'#MENU_CREATE')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//a[contains(@href,'#MENU_CREATE')]"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] SEARCH CREATE");
		}
	}
		return null;
	}
  
    //STEP 4
	public static WebElement request_submenu() {
		for (int x = 0; x < 5; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.xpath("//a[contains(@href,'#CREATE_REQUEST')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//a[contains(@href,'#CREATE_REQUEST')]"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] REQUEST SUB-MENU");
		}
	  }
		return null;
	}
	
	public static WebElement submenu_wfm() {
		for (int x = 0; x < 5; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("CREATE_REQUEST:@WFM Position Level Management");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			return elem;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] WFM Position Level Management sub-menu");
		}
	  }
		return null;
	}

	public static WebElement submit() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("submit");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			return elem;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] SUBMIT BUTTON");
		}
	  }
		return null;
	}
	
	private static void ongoingUpate() {
		for (int x = 0; x < 20; x++) {
		try {
			Thread.sleep(2000);
			endRec = Instant.now();timeElapsedRec = Duration.between(startRec, endRec);
			 if (!error.isEmpty()) {
						 update.setString(1, "[ERROR] Invalid data: " + error);
						 reqID = "SKIPPED";
						 currentStatus = "[DATA ISSUE] "+error;
				}else {
				Thread.sleep(1000);
				currentStatus = statusWait(); 
				Thread.sleep(1000);
				reqID = getReqIDt().getAttribute("innerText");
				update.setString(1, "DONE");	
			 }

			 update.setString(2, reqID);
	         update.setString(3, currentStatus);
	         update.setLong(4, timeElapsedRec.toMillis());
	         update.setString(5, thread);
	         update.setString(6, id);
	         Thread.sleep(2000);
	         break;
		} catch (Exception e) {
			System.out.println("[RETRY] UPDATE FAILED");
		}
		}		
	}
	
//	
	private static void ongoingUpateWithReqId() {
		for (int x = 0; x < 20; x++) {
		try {
			Thread.sleep(2000);
			endRec = Instant.now();timeElapsedRec = Duration.between(startRec, endRec);
			 if (!error.isEmpty()) {
				 update.setString(1, error);
				}else {
				update.setString(1, "DONE");	
			 }

			 reqID = getReqIDt().getAttribute("innerText");
			 update.setString(2, reqID);
	         update.setString(3, currentStatus);
	         update.setLong(4, timeElapsedRec.toMillis());
	         update.setString(5, thread);
	         update.setString(6, id);
	         Thread.sleep(2000);
	         break;
		} catch (Exception e) {
			System.out.println("[RETRY] UPDATE FAILED");
		}
		}		
	}
	
	public static WebElement reqID() {
		for (int x = 0; x < 20; x++) {
			System.out.println("Reached");
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.xpath("//*[contains(@href,'RequestDetail.jsp?REQUEST_ID=')]");
			
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//*[contains(@href,'RequestDetail.jsp?REQUEST_ID=')]"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] REQUEST ID LINK");
		}
		}
		return null;
	}
	
	
	
	public static WebElement completePLM() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.xpath("//a//div[contains(text(), 'Complete PLM')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//a//div[contains(text(), 'Complete PLM')]"));
			System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [COMPLETE PLM]");
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] COMPLETE PLM BUTTON");
		}
		}
		return null;
	}

	public static WebElement releaseForAppvl() {
		for (int x = 0; x < 20; x++) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			By elemPath = By.xpath("//a//div[contains(text(), 'Release for Approval')]");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.xpath("//a//div[contains(text(), 'Release for Approval')]"));
			System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [RELEASE FOR APPROVAL]");
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] RELEASE FOR APPROVAL BUTTON");
		}
		}
		return null;
	}
	
	
	public static WebElement getReqIDt() {
		for (int x = 0; x < 20; x++) { 
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("DRIVEN_REQUEST_ID");
			wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			WebElement element = driver.findElement(By.id("DRIVEN_REQUEST_ID"));
			return element;
		} catch (Exception e) {
			driver.navigate().refresh();
			System.out.println("[WAITING] REQUEST ID");
		}
		}
		return null;
	}
	
	public static WebElement projID() {
		for (int x = 0; x < 20; x++) { 
		try {
			Thread.sleep(1000);
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("REQD.P.WFM_PROJECT_IDAC_TF");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.id("REQD.P.WFM_PROJECT_IDAC_TF"));
			return element;
		} catch (Exception e) {
			System.out.println("[WAITING] PROJECT ID FIELD");
		}
		}
		return null;
	}
	
	private static String projmanagerStr() {
		for (int x = 0; x < 20; x++) {
			try {
				WebDriverWait wait = new WebDriverWait(driver, 5);
				By elemPath = By.id("DRIVEN_P_10");// project manager 
				WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
				String elemText = wait.until(ExpectedConditions.elementToBeClickable(elem)).getText().toString();
				return elemText;
				} catch (Exception e) {
					System.out.println("[WAITING] PROJECT MANAGER VALUE");
			}
		}
		return null;
	}
	
	public static WebElement forecastEdit() throws Throwable {
		for (int x = 0; x < 20; x++) { 
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			By elemPath = By.id("BT_EDIT_P_1");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.id("BT_EDIT_P_1"));
			return element;
		} catch (Exception e) {
			System.out.println("[WAITING] EDIT BUTTON");
			focastneededExpand().click();Thread.sleep(1000);	
			expandAll();Thread.sleep(1000);	
		}
		}
		return null;
	}
	
	public static WebElement focastneededExpand() {
		for (int x = 0; x < 20; x++) { 
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);			
			By elemPath = By.id("IMAGE_EC_REQUEST_TC_REQD.P.WFM_FORECAST_NEEDED");
			WebElement elem = wait.until(ExpectedConditions.presenceOfElementLocated(elemPath));
			wait.until(ExpectedConditions.elementToBeClickable(elem));
			WebElement element = driver.findElement(By.id("IMAGE_EC_REQUEST_TC_REQD.P.WFM_FORECAST_NEEDED"));
			return element;
		} catch (Exception e) {
			System.out.println("[WAITING] CAN NOT EXPAND");
			expandAll();
		}
		}
		return null;
	}

	
	public static void expandAll() {
		try {
			List<WebElement> collapseExpand = driver.findElements(By.xpath("//img[contains(@alt,'Expand')]"));
			if (collapseExpand.size() > 0) {
				for (WebElement elem: collapseExpand) {
					elem.click();
				}}else {};
		} catch (Exception e) {
		}
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
		public static void alertHandler() {
			for (int x = 0; x < 3; x++) {
			try {
				WebDriverWait wait = new WebDriverWait(driver, 1);
				Alert alert = wait.until(ExpectedConditions.alertIsPresent());
				String alertmessage = alert.getText();
				alert.accept();
				System.out.println("[ALERT MESSAGE]:"+alertmessage);
			    error=alertmessage;
				return;
			} catch (Exception e) {
			}
		   }
		
	}
		
		public static void alertHandlermenu() {
			for (int x = 0; x < 3; x++) {
			try {
				WebDriverWait wait = new WebDriverWait(driver, 1);
				Alert alert = wait.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				break;
			} catch (Exception e) {
			}
		   }
		
	}
	
		
	
		public static void invalidataHandler(String dataIssue) {
			try {
				Thread.sleep(3000);
				List<WebElement> iframe=driver.findElements(By.tagName("iframe"));
				 for(int i=0; i<=iframe.size(); i++){
					 if (iframe.get(i).getAttribute("id").equals("autoCompleteDialogIF")){
						 	Thread.sleep(1000);
						    iframe.get(i).sendKeys(Keys.ESCAPE);
					        Thread.sleep(1000);
					      error=dataIssue;
					     System.out.println("RECORD ["+id+"] - PROJECT ID ["+projIDStr+"] >> [DATA ISSUE]");
						 break;
					 }
				 }
			} catch (Exception e) {
			}
		 }
	
		
	
}

