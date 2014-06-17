package com.joelchristophel;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BrowserUtilities {

	private FirefoxDriver driver;

	public BrowserUtilities(FirefoxDriver driver) {
		this.driver = driver;
	}

	public WebElement checkForElement(By locator, int giveUpAfter, int implicitWaitDefault, TimeUnit timeUnit) {
		driver.manage().timeouts().implicitlyWait(giveUpAfter, timeUnit);
		List<WebElement> elements = driver.findElements(locator);
		driver.manage().timeouts()
				.implicitlyWait(implicitWaitDefault, timeUnit);

		if (elements.size() > 0) {
			return elements.get(0);
		}

		return null;
		
	}
	
	public void waitUntilUrlDoesntContain(String text) {
		while(driver.getCurrentUrl().toLowerCase().contains(text.toLowerCase())) {
			System.out.println(driver.getCurrentUrl());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String waitUntilUrlContains(String... texts) {
		while(true) {
		for(String text : texts) {
			if (driver.getCurrentUrl().toLowerCase().contains(text.toLowerCase())) {
				return driver.getCurrentUrl();
			}
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}
	}

	public boolean retryingFindClick(By by) {
		boolean result = false;
		int attempts = 0;
		while (attempts < 2) {
			try {
				driver.findElement(by).click();
				result = true;
				break;
			} catch (StaleElementReferenceException e) {
			}
			attempts++;
		}
		return result;
	}

	public void closeTab(int tab) {
		closeTab(tab, "");
	}

	public void closeTab(int tab, String urlPhrase) {
		do {
			switchToTab(tab);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (!driver.getCurrentUrl().contains(urlPhrase));

		sendKeys(Keys.chord(Keys.CONTROL, "w"));
	}

	public void openNewWindow(String url) {
		JavascriptExecutor js = driver;
		js.executeScript("window.open()", "");
		switchWindow();
		driver.get(url);
	}

	public void openNewTab() {
		sendKeys(Keys.chord(Keys.CONTROL, "t"));
	}

	public void switchToTab(int tab) {
		if (tab >= 0 && tab <= 8) {

			Keys numpad = null;

			switch (tab) {
			case 0:
				numpad = Keys.NUMPAD1;
				break;
			case 1:
				numpad = Keys.NUMPAD2;
				break;
			case 2:
				numpad = Keys.NUMPAD3;
				break;
			case 3:
				numpad = Keys.NUMPAD4;
				break;
			case 4:
				numpad = Keys.NUMPAD5;
				break;
			case 5:
				numpad = Keys.NUMPAD6;
				break;
			case 6:
				numpad = Keys.NUMPAD7;
				break;
			case 7:
				numpad = Keys.NUMPAD8;
				break;
			case 8:
				numpad = Keys.NUMPAD9;
				break;
			}

			sendKeys(Keys.chord(Keys.CONTROL, numpad));
		}
	}

	public void switchWindow() {
		Set<String> handles = driver.getWindowHandles();
		String current = driver.getWindowHandle();
		handles.remove(current);
		String newTab = handles.iterator().next();
		driver.switchTo().window(newTab);
	}

	public void sendKeys(CharSequence sequence) {
		if (driver.getCurrentUrl().equalsIgnoreCase("about:blank")) {
			new Actions(driver).sendKeys(sequence).perform();
		} else {
			new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
			driver.findElementByTagName("body").sendKeys(sequence);
		}
	}
	
	public static String getRandomString() {
		String string = "";
		int stringLength = randomInt(8, 15);

		for (int i = 0; i < stringLength; i++) {
			int randomInt = randomInt(0, 35);

			if (randomInt <= 9) {
				string += randomInt;
			} else {
				string += (char) randomInt(97, 122);
			}
		}

		return string;
	}
	
	public static int randomInt(int minimum, int maximum) {
		return minimum + (int) (Math.random() * ((maximum - minimum) + 1));
	}
}