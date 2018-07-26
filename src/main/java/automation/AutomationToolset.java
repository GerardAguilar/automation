package automation;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
//import org.sikuli.script.Screen;

import com.google.common.base.Function;


/***
 * AutomationToolset needs to be easily adaptable by other Fixtures
 * For example, it should be clean enough to be useable to a website, an open canvas project, and an rtp project.
 * That doesn't mean have all possible methods, create methods as needed
 * Need to consider:
 * -Aside from initialization, should all methods be independent of each other?
 * External libs:
 * -Selenium Webdriver
 * -Sikulix
 * Notes:
 * -As most of these methods need to be accessible via Fitnesse, we'll have to keep them public
 * @author gaguilar
 * -Will only use Chrome for now
 */
public class AutomationToolset {
	public WebDriver driver;
	public WebDriverWait wait;
//	public Screen s = new Screen();
	public String instanceStartTime;
	public ArrayList<String> navigationPath;
	public String cwd;
	public String chromeBinaryLocation;
	public String xpath;	
	public String setupCommand;	
	public String notes;
	public PrintWriter out;
	public boolean baselineSet = false;
	public String loc;		
	
	public boolean willSimulateClick;
	public String willSimulateNavigation;	
	public boolean willTakeBaselineSet;
	public boolean willEndScreening;
	public String willSimulateDropdownSelect;
	public String willWaitForExpectedConditionType;
	public String willWaitForJavascriptExecutorString;
//	public String willWaitForSelectDropdownOptions;
	
	public AutomationToolset() {
		try {
//			initialize("about:blank");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void initialize() {	
		System.out.println("initialize()");
	    navigationPath = new ArrayList<String>();
	    navigationPath = new ArrayList<String>();
	    instanceStartTime = (new Timestamp(System.currentTimeMillis())).getTime()+"";		
	    cwd  = new File("").getAbsolutePath();
	    
        File baselineDirectory = new File(cwd+"\\baseline");
        if (!baselineDirectory.exists()) {
        	baselineDirectory.mkdirs();
        }
       
        File currentDirectory = new File(cwd+"\\current\\"+instanceStartTime);
        if (!currentDirectory.exists()) {
        	currentDirectory.mkdirs();
        }
        
	    try {
			setupChrome();
		} catch (Exception e) {
			e.printStackTrace();
		}	    

	    wait = new WebDriverWait(driver, 20);
	    System.out.println("end initialize()");
	}
	
	public void setWillSimulateNavigation(String temp) {
		willSimulateNavigation = temp;
	}
	public String getWillSimulateNavigation(String temp) {
		return willSimulateNavigation;
	}
	public void setWillSimulateClick(boolean temp) {
		willSimulateClick = temp;
	}
	public boolean getWillSimulateClick() {
		return willSimulateClick;
	}
	
	public void setWillWaitForExpectedConditionType(String temp) {
		willWaitForExpectedConditionType = temp;
	}
	public String getWillWaitForExpectedConditionType() {
		return willWaitForExpectedConditionType;
	}
	public void setWillSimulateDropdownSelect(String temp) {
		willSimulateDropdownSelect = temp;
	}
	public String getWillSimulateDropdownSelect() {
		return willSimulateDropdownSelect;
	}
	public void setWillTakeBaselineSet(boolean temp) {
		willTakeBaselineSet = temp;
	}	
	public boolean getWillTakeBaselineSet() {
		return willTakeBaselineSet;
	}
	public void setWillEndScreening(boolean temp) {
		willEndScreening = temp;
	}
	public boolean getWillEndScreening() {
		return willEndScreening;
	}
//	public void setWillWaitForSelectDropdownOptions(String temp) {
//		willWaitForSelectDropdownOptions = temp;
//	}
//	public String getWillWaitForSelectDropdownOptions() {
//		return willWaitForSelectDropdownOptions;
//	}
	
	public void setNotes(String temp) {
		notes = temp;
	}
	public String getNotes() {
		return notes;
	}
	public void setXpath(String temp) {
		xpath = temp;
	}
	public String getXpath() {
		return xpath;
	}
	public void setSetupCommand(String temp) {
		setupCommand = temp;
	}
	public String getSetupCommand() {
		return setupCommand;
	}
	public void setChromeBinaryLocation(String temp) {
		chromeBinaryLocation = temp;
	}
	public String getChromeBinaryLocation() {
		return chromeBinaryLocation;
	}
	public void setWillWaitForJavascriptExecutorString(String temp) {
		willWaitForJavascriptExecutorString = temp;
	}
	public String getWillWaitForJavascriptExecutorString() {
		return willWaitForJavascriptExecutorString;
	}
			
	public void simulate() {
		navigateByGlobalAddress();
		clickElementByXpath();
		selectFromDropdown();
	}
	
	public void waitForSelectDropdownOptions() {
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
			    .withTimeout(6, TimeUnit.SECONDS)
			    .pollingEvery(1, TimeUnit.SECONDS)
			    .ignoring(NoSuchElementException.class);
		
		wait.until(new Function<WebDriver, Boolean>() 
		{
			public Boolean apply(WebDriver driverCopy) {
				Select select = new Select(driver.findElement(By.xpath(xpath)));
				int count = select.getOptions().size();
				boolean selectHasOptions = count>1;
	            System.out.println("count: " + count);
	            return selectHasOptions;
			}
		});	
		
	}
	
	//Waits for source code to stabilize and be identical, needs testing and may need other waits (CSS, JQuery, ExpectedConditions)
	//May also need to make the timeout value parameterized
	public void waitForIdenticalPageSources() {	
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
			    .withTimeout(6, TimeUnit.SECONDS)
			    .pollingEvery(1, TimeUnit.SECONDS)
			    .ignoring(NoSuchElementException.class);

		wait.until(new Function<WebDriver, Boolean>() 
		{
			public Boolean apply(WebDriver driverCopy) {
				boolean sameSource = false;
				try {
					String pageSourceFirst = driverCopy.getPageSource();
					Thread.sleep(100);
					String pageSourceSecond = driverCopy.getPageSource();
					if(pageSourceSecond.equals(pageSourceFirst)) {
						sameSource=true;
					}			
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return sameSource;
			}
		});	
	}
	
	//Wait for image to appear
		public void waitFor() {
			if(xpath.length()>0) {
//				System.out.println("actually locateElementInPageByXpathAndWaitForNonZeroWidth: " + xpath + "_wait");
				addActionToNavigationPath("-wait for " + xpath);	

				Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)			    
						.withTimeout(12, TimeUnit.SECONDS)
					    .pollingEvery((long) .5, TimeUnit.SECONDS)
					    .ignoring(NoSuchElementException.class)
						.ignoring(ElementNotVisibleException.class);
				
				JavascriptExecutor jseWait = (JavascriptExecutor)driver;
				Wait<JavascriptExecutor> waitJse = new FluentWait<JavascriptExecutor>(jseWait)
					    .withTimeout(6, TimeUnit.SECONDS)
					    .pollingEvery(2, TimeUnit.SECONDS)
					    .ignoring(NoSuchElementException.class)
					    .ignoring(ElementNotVisibleException.class);
				
				String[] splitXpath = xpath.split("/");
				if(splitXpath[splitXpath.length-1].contains("select")) {
					waitForSelectDropdownOptions();					
				}
				
				if(willWaitForExpectedConditionType.length()>0) {
					willWaitForExpectedConditionType = willWaitForExpectedConditionType.toLowerCase();
					switch(willWaitForExpectedConditionType) {
						case "":
//							System.out.println("Expected Condition Type is empty");
							break;
						case "elementtobeselected":
							wait.until(ExpectedConditions.elementToBeSelected(By.xpath(xpath)));
							System.out.println("wait elementtobeselected end");
							break;
						case "elementtobeclickable":
							wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
							System.out.println("wait elementtobeclickable end");
							break;
						case "visibilityofelementlocated":
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
							System.out.println("wait visibilityofelementlocated end");
							break;
						case "invisibilityofelementlocated":
							wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
							System.out.println("wait invisibilityofelementlocated end");
							break;
						case "presenceOfElementLocated":
							wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
							System.out.println("wait presentOfElementLocated");
							WebElement elCopy = driver.findElement(By.xpath(xpath));
							System.out.println(elCopy.getText());
							break;
						default:
//							System.out.println("Unable to determine expected condition type: " + willWaitForExpectedConditionType);
							break;
					}
				}
//				if(willWaitForJavascriptExecutorString.length()>0) {				
//					
//					final String javascriptExecutorStringCopy = willWaitForJavascriptExecutorString;
//					waitJse.until(new Function<JavascriptExecutor, Boolean>() 
//					{					
//						public Boolean apply(JavascriptExecutor jseCopy) {
//							boolean javascriptStatus = false;
//							if(javascriptExecutorStringCopy.equals("return document.readyState")) {
//								javascriptStatus = jseCopy.executeScript("return document.readyState").toString().equals("complete");
//							}
//							else {
//								//this is untested, and will have to catch a number of other common results that gets returned from running Javascript
//								//will need a javascript validator (but for now, just provide a set of common commands
//								//WARNING: This can also break the application, as running any string as javascript grants user access to the insides of the targetted application
//								javascriptStatus = (boolean)jseCopy.executeScript(javascriptExecutorStringCopy);
//							}
//							return javascriptStatus;
//						}
//					});	
//				}		

			}

		}
	
	
	//Navigate
	public void navigateByGlobalAddress() {
		if(willSimulateNavigation.length()>0) {
			driver.get(willSimulateNavigation);
			addActionToNavigationPath("-navigate to " + willSimulateNavigation);
		}
	}
	
	//Click
	public void clickElementByXpath() {
		if(xpath.length()>0 && willSimulateClick) {
			WebElement element = driver.findElement(By.xpath(xpath));			
			addActionToNavigationPath("-click " + xpath);
			element.click();
		}
	}
	
	//Select
	public void selectFromDropdown() {
		//maybe some kind of newline/tab delimiters?
		//Excel would be best
		System.out.println(willSimulateDropdownSelect + " | " + xpath);
		if(willSimulateDropdownSelect.length()>0) {			
			WebElement mySelectElement = driver.findElement(By.xpath(xpath));
			Select dropdown = new Select(mySelectElement);
			List<WebElement> els = dropdown.getOptions();
			for(int i=0; i<els.size(); i++) {
				System.out.print(els.get(i).toString());	
				System.out.println(els.get(i).getAttribute("innerHTML"));
			}
			dropdown.selectByVisibleText(willSimulateDropdownSelect);
//			dropdown.selectByValue(willSimulateDropdownSelect);
//			dropdown.selectByIndex(1);
		}
	}
	//Execute
	public void executeCommand() {
		setupCommand = setupCommand.toLowerCase();
		switch(setupCommand) {
			case "initialize baseline":
				try {
					willTakeBaselineSet = true;
					initialize();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "initialize":
				try {
					willTakeBaselineSet = false;
					initialize();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "end":
				willEndScreening = true;
				break;
			case "":
				break;
			default:
//					System.out.println("Invalid command: " + setupCommand);
				break;
		}
	}
	
	//pairs chrome driver with chrome binary
	private void setupChrome() throws Exception{
		if(chromeBinaryLocation.length()>0) {
			/***
			 * From our resources folder, copy chromedriver.exe into a Driver folder
			 * Modify that chrome driver to attach to the chrome binary as designated in the Fitnesse table
			 */
			ClassLoader classLoader = getClass().getClassLoader();
	        URL resource = classLoader.getResource("chromedriver.exe");
//			File chromedriver = new File("Driver"+"\\chromedriver.exe");//this seems to have issues being created when triggered from a jar file, likely due to the location
			File chromedriver = new File("chromedriver.exe");
			System.out.println("Location of chromedriver.exe is " + chromedriver.getAbsolutePath());
            if (!chromedriver.exists()) {
            	chromedriver.createNewFile();
                FileUtils.copyURLToFile(resource, chromedriver);
            }else {
            	System.out.println("chromedriver.exe already exists");
            }
			String chromeDriverLocation = chromedriver.getAbsolutePath();
	        
			ChromeOptions options = new ChromeOptions();
			options.setBinary(chromeBinaryLocation);
			options.addArguments("disable-infobars");
//			options.addArguments("--allow-file-access-from-files");			
			System.setProperty("webdriver.chrome.driver", chromeDriverLocation);              
			driver = new ChromeDriver(options);
		    driver.get("about:blank");
		    driver.manage().window().maximize(); 
		 
		    
			addActionToNavigationPath("[Initialize " + chromeDriverLocation + "]\r\n");
		}
		
	}
	
	//used to keep track of actions
	public void addActionToNavigationPath(String action) {
		String str;
		int size = navigationPath.size();
		if(size==0) {
			str = action;
			navigationPath.add("\r\n\t"+str);		
//			populateLogFile(navigationPath);
		}else {
			str = navigationPath.get(size-1)+"\r\n\t"+action;
			navigationPath.add(str);
//			populateLogFile(navigationPath);
		}
	}
	
	public void createLogFile() {
        try {
        	loc ="";
        	if(baselineSet) {
        		loc = cwd+"\\baseline\\log.txt";
        	}else {
        		loc = cwd+"\\current\\"+instanceStartTime+"\\log.txt";
        	}
        	out = new PrintWriter(loc);
	        out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void populateLogFile(ArrayList<String> navArray) {
		String appendedIndex = String.format("%04d", navArray.size()-1);
		//open out for appending
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(loc, true)));
	    	out.append("\r\n" + appendedIndex + ": " + navigationPath.get(navArray.size()-1));
	    	out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}    	
	}
	
//	//TODO postSimulation()
//	public String postSimulation() {
//		
////		takeScreenshotAndGenerateDiff();
//		//image
//		if(willEndScreening) {
//			endScreening();
//			return ("Finished");
//		}
//		else {
//			return Image();
//		}
//	}
	
//	public String Image() {
//		String currentImageId = getNavigationPathAltEventId()+"";
//		String currentImagePath = getLastSetOfActions(navigationPathAlternate, 1);
//		//if interacting, hide the image
//		if(willSimulateClick
//				||willSimulateHover
//				||willSimulateNavigation.length()>0
//				||willGenerateNewTab
//				||(willSimulatePageScrollPosition.length()>0)) {
//			return "<div><details><summary>"+currentImageId+"</summary><p>"+currentImagePath+"</p></details></div><div><details><summary>Image</summary><img src='http://localhost/files/"+currentImageId+".png' height='150'></details>Element click may have caused movement. Verify next screenshot for result.</div>";
//		}else {
//			return "<div><details><summary>"+currentImageId+"</summary><p>"+currentImagePath+"</p></details></div><div><img src='http://localhost/files/"+currentImageId+".png' height='150'></div>";
//		}	
//	}
	
//	public void endScreening() {
//		out.close();
//		driver.close();
//	}
	
	public static void main(String[] args) {
		AutomationToolset temp = new AutomationToolset();
	}

	

}
