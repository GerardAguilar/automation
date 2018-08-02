package automation;


import java.awt.Color;
import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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
	public String elementId;
	public String customAttributeIdPair;
	public String setupCommand;	
	public String notes;
	public PrintWriter out;
//	public boolean baselineSet = false;
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
	public void setElementId(String temp) {
		elementId = temp;
	}
	public String getELementId() {
		return elementId;
	}
	public void setCustomAttributeIdPair(String temp) {
		customAttributeIdPair = temp;
	}
	public String getCustomAttributeIdPair() {
		return customAttributeIdPair;
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
		clickElement();
		selectFromDropdown();
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
		if(xpath.length()>0 || elementId.length()>0 || customAttributeIdPair.length()>0) {
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
			
			if (customAttributeIdPair.length()>0 && (willSimulateDropdownSelect.length()>0)) {
				addActionToNavigationPath("-wait for " + "*["+customAttributeIdPair+"]");			
				
				wait.until(new Function<WebDriver, Boolean>(){
					public Boolean apply(WebDriver driverCopy) {
						System.out.println("waitFor(): " + customAttributeIdPair);
						Boolean elementIsPresent = false;
						WebElement tempElement = driver.findElement(By.cssSelector("*["+customAttributeIdPair+"]"));
						if(tempElement!=null) {
							elementIsPresent = true;
						}else {
							elementIsPresent = false;
						}
						return elementIsPresent;						
					}
				});		
				
				WebElement tempElement = driver.findElement(By.cssSelector("*["+customAttributeIdPair+"]"));				
				String tempAttr = tempElement.getAttribute("type");
				
				if(tempAttr.equals("select")) {
					wait.until(new Function<WebDriver, Boolean>() 
					{
						public Boolean apply(WebDriver driverCopy) {
							Select select = new Select(driver.findElement(By.id(elementId)));
							int count = select.getOptions().size();
							boolean selectHasOptions = count>1;
				            System.out.println("count: " + count);
				            return selectHasOptions;
						}
					});				
				}				
				
				if(willWaitForExpectedConditionType.length()>0) {
					willWaitForExpectedConditionType = willWaitForExpectedConditionType.toLowerCase();
					switch(willWaitForExpectedConditionType) {
						case "":
//								System.out.println("Expected Condition Type is empty");
							break;
						case "elementtobeselected":
							wait.until(ExpectedConditions.elementToBeSelected(By.cssSelector("*["+customAttributeIdPair+"]")));
							System.out.println("wait elementtobeselected end");
							break;
						case "elementtobeclickable":
							wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("*["+customAttributeIdPair+"]")));
							System.out.println("wait elementtobeclickable end");
							break;
						case "visibilityofelementlocated":
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("*["+customAttributeIdPair+"]")));
							System.out.println("wait visibilityofelementlocated end");
							break;
						case "invisibilityofelementlocated":
							wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("*["+customAttributeIdPair+"]")));
							System.out.println("wait invisibilityofelementlocated end");
							break;
						case "presenceOfElementLocated":
							wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("*["+customAttributeIdPair+"]")));
							System.out.println("wait presentOfElementLocated");
							WebElement elCopy = driver.findElement(By.id(elementId));
							System.out.println(elCopy.getText());
							break;
						default:
//							System.out.println("Unable to determine expected condition type: " + willWaitForExpectedConditionType);
							break;
					}
				}
			}
			else if (elementId.length()>0) {
				addActionToNavigationPath("-wait for " + elementId);	
			
				WebElement tempElement = driver.findElement(By.id(elementId));
				String tempAttr = tempElement.getAttribute("type");
				if(tempAttr.equals("select")) {
					wait.until(new Function<WebDriver, Boolean>() 
					{
						public Boolean apply(WebDriver driverCopy) {
							Select select = new Select(driver.findElement(By.id(elementId)));
							int count = select.getOptions().size();
							boolean selectHasOptions = count>1;
				            System.out.println("count: " + count);
				            return selectHasOptions;
						}
					});				
				}
				
				
				if(willWaitForExpectedConditionType.length()>0) {
					willWaitForExpectedConditionType = willWaitForExpectedConditionType.toLowerCase();
					switch(willWaitForExpectedConditionType) {
						case "":
//								System.out.println("Expected Condition Type is empty");
							break;
						case "elementtobeselected":
							wait.until(ExpectedConditions.elementToBeSelected(By.id(elementId)));
							System.out.println("wait elementtobeselected end");
							break;
						case "elementtobeclickable":
							wait.until(ExpectedConditions.elementToBeClickable(By.id(elementId)));
							System.out.println("wait elementtobeclickable end");
							break;
						case "visibilityofelementlocated":
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(elementId)));
							System.out.println("wait visibilityofelementlocated end");
							break;
						case "invisibilityofelementlocated":
							wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(elementId)));
							System.out.println("wait invisibilityofelementlocated end");
							break;
						case "presenceOfElementLocated":
							wait.until(ExpectedConditions.presenceOfElementLocated(By.id(elementId)));
							System.out.println("wait presentOfElementLocated");
							WebElement elCopy = driver.findElement(By.id(elementId));
							System.out.println(elCopy.getText());
							break;
						default:
//							System.out.println("Unable to determine expected condition type: " + willWaitForExpectedConditionType);
							break;
					}
				}
			}
			else if(xpath.length()>0) {
				addActionToNavigationPath("-wait for " + xpath);	
			
				String[] splitXpath = xpath.split("/");
				if(splitXpath[splitXpath.length-1].contains("select")) {						
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
				
				if(willWaitForExpectedConditionType.length()>0) {
					willWaitForExpectedConditionType = willWaitForExpectedConditionType.toLowerCase();
					switch(willWaitForExpectedConditionType) {
						case "":
//								System.out.println("Expected Condition Type is empty");
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
//								System.out.println("Unable to determine expected condition type: " + willWaitForExpectedConditionType);
							break;
					}
				}
			}
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
	public void clickElement() {
		if(willSimulateClick) {
			if(customAttributeIdPair.length()>0) {
				WebElement element = driver.findElement(By.cssSelector("*["+customAttributeIdPair+"]"));			
				addActionToNavigationPath("-click " + elementId);
				element.click();
			}
			else if(elementId.length()>0) {
				WebElement element = driver.findElement(By.id(elementId));			
				addActionToNavigationPath("-click " + elementId);
				element.click();
			}
			else if(xpath.length()>0) {
				WebElement element = driver.findElement(By.xpath(xpath));			
				addActionToNavigationPath("-click " + xpath);
				element.click();
			}
		}

	}
	
	//Select
	public void selectFromDropdown() {
		//maybe some kind of newline/tab delimiters?
		//Excel would be best
		if(willSimulateDropdownSelect.length()>0) {
			if(customAttributeIdPair.length()>0) {			
				WebElement mySelectElement = driver.findElement(By.cssSelector("*["+customAttributeIdPair+"]"));
				Select dropdown = new Select(mySelectElement);
//				List<WebElement> els = dropdown.getOptions();
//				for(int i=0; i<els.size(); i++) {
//					System.out.print(els.get(i).toString());	
//					System.out.println(els.get(i).getAttribute("innerHTML"));
//				}
				dropdown.selectByVisibleText(willSimulateDropdownSelect);
			}
			else if(elementId.length()>0) {			
				WebElement mySelectElement = driver.findElement(By.id(elementId));
				Select dropdown = new Select(mySelectElement);
//				List<WebElement> els = dropdown.getOptions();
//				for(int i=0; i<els.size(); i++) {
//					System.out.print(els.get(i).toString());	
//					System.out.println(els.get(i).getAttribute("innerHTML"));
//				}
				dropdown.selectByVisibleText(willSimulateDropdownSelect);
			}
			else if(xpath.length()>0) {
				WebElement mySelectElement = driver.findElement(By.xpath(xpath));
				Select dropdown = new Select(mySelectElement);
//				List<WebElement> els = dropdown.getOptions();
//				for(int i=0; i<els.size(); i++) {
//					System.out.print(els.get(i).toString());	
//					System.out.println(els.get(i).getAttribute("innerHTML"));
//				}
				dropdown.selectByVisibleText(willSimulateDropdownSelect);
			}
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
        	if(willTakeBaselineSet) {
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
	
	//TODO postSimulation()
	public String postSimulation() {		
		takeScreenshot();
		if(willEndScreening) {
			return "Finished";
		}else {
			return Image();
		}		
	}
	
	@SuppressWarnings("unused")
	public void takeScreenshot() {
		File screenshotFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		Long timestamp = (new Timestamp(System.currentTimeMillis())).getTime();
		String navId = getNavigationPathEventId()+"";
		String fitnesseRootFileDirectory = "C:\\eclipse-workspace\\automation\\FitNesseRoot\\files\\";
		String currentFilename = fitnesseRootFileDirectory+""+navId+".png";
		
		try {
			FileUtils.copyFile(screenshotFile, new File(currentFilename));					
		} catch (IOException e) {
			e.printStackTrace();
		}	
				
	}
	
	/***
	 * Calls getDifferenceImage() and writes the image into an outputfile	 * 
	 * @param baselineImage
	 * @param newImage
	 * @param someLocation
	 */
	public void compareBaseLineWithImmediateScreenshot(BufferedImage baselineImage, BufferedImage newImage, String diffFileName) {
		BufferedImage diff = getDifferenceImage(baselineImage, newImage);
		File outputfile = new File(diffFileName); 
		try {
			ImageIO.write(diff, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unused")
	public static BufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
	    BufferedImage outImg;
	    int width2 = img2.getWidth(); // take no arguments
	    int height2 = img2.getHeight();
	    //if there's no baseline image
	    if(img1==null) {
	    	outImg = new BufferedImage(width2, height2, BufferedImage.TYPE_INT_RGB);
	        for(int i=0; i<height2; i++) {
	        	for(int j=0; j<width2; j++) {
	        		int lightGrey =  Color.LIGHT_GRAY.getRGB();
	        		outImg.setRGB(j, i, lightGrey);	   
	        	}
	        }
	    }else {
		    int width1 = img1.getWidth(); // Change - getWidth() and getHeight() for BufferedImage
		    int height1 = img1.getHeight();
	    	
	    	//if the screenshot image does not have the same dimensions as the baseline image
	    	if ((width1 != width2) || (height1 != height2)) {
	    		//use the smaller of each dimension to create the outimg
	    		int smallerWidth = Math.min(width1,  width2);
	    		int smallerHeight = Math.min(height1, height2);
	    		outImg = new BufferedImage(smallerWidth, smallerHeight, BufferedImage.TYPE_INT_RGB);
		        System.err.println("Error: Images dimensions mismatch");
		        //change diff output to grey
		        for(int i=0; i<smallerHeight; i++) {
		        	for(int j=0; j<smallerWidth; j++) {
//		        		int white = img1.getRGB(j,i)*100;
		        		int blue =  Color.BLUE.getRGB();
		        		outImg.setRGB(j, i, blue);	   
		        	}
		        }
		    //finally, if the baseline image exists and the screenshot image has the same dimensions.
		    }else {
		    	outImg = new BufferedImage(width1, height1, BufferedImage.TYPE_INT_RGB);
			    // Modified - Changed to int as pixels are ints
			    int diff;
			    int result; // Stores output pixel
			    for (int i = 0; i < height1; i++) {
			        for (int j = 0; j < width1; j++) {
			            int rgb1 = img1.getRGB(j, i);
			            int rgb2 = img2.getRGB(j, i);
			            int r1 = (rgb1 >> 16) & 0xff;
			            int g1 = (rgb1 >> 8) & 0xff;
			            int b1 = (rgb1) & 0xff;
			            int r2 = (rgb2 >> 16) & 0xff;
			            int g2 = (rgb2 >> 8) & 0xff;
			            int b2 = (rgb2) & 0xff;
			            diff = Math.abs(r1 - r2);
			            diff += Math.abs(g1 - g2);
			            diff += Math.abs(b1 - b2);
//			            diff /= 3; // Change - Ensure result is between 0 - 255
			            // Make the difference image gray scale
			            // The RGB components are all the same
			            result = (diff << 16) | (diff << 8) | diff;
			            outImg.setRGB(j, i, result); // Set result
			        }
			    }
		    }
	    }
	    return outImg;
	}
	
	public String Image() {
		String currentImageId = getNavigationPathEventId()+"";
		String currentImagePath = getLastSetOfActions(navigationPath, 1);
		return "<div><details><summary>"+currentImageId+"</summary><p>"+currentImagePath+"</p></details></div><div><img src='http://localhost/files/"+currentImageId+".png' height='150'></div>";
	}	
	
	public int getNavigationPathEventId() {
		int eventId = navigationPath.size()-1;
		return eventId;
	}
	
	protected String getLastSetOfActions(ArrayList<String> navArray, int count) {		
		int arraySize = navArray.size();
		String temp=navArray.get(arraySize-1);
		String tempArray[] = temp.split("\n");
		int tempSize = tempArray.length;
		return temp;
	}
	
	public void endScreening() {
		out.close();
		driver.close();
	}
	
	public static void main(String[] args) {
		AutomationToolset temp = new AutomationToolset();
	}

	

}
