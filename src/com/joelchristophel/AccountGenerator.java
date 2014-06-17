package com.joelchristophel;

import java.util.Calendar;

import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import static org.junit.Assert.*;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccountGenerator {
	private FirefoxDriver driver;
	private BrowserUtilities utilities;
	private int IMPLICIT_WAIT_SECONDS = 60;
	private StringBuffer verificationErrors = new StringBuffer();
	private String currentCreationUrl;
	private Logger log;
	private static String errorMessage = "A problem was encountered. Details are in the log.";
	private static final String CREATION_URL = "https://secure.runescape.com/m=account-creation/create_account";
	private final String EMAIL_WEBSITE = build(104, 116, 116, 112, 115, 58, 47, 47, 119, 119, 119, 46, 103, 117, 101,
			114, 114, 105, 108, 108, 97, 109, 97, 105, 108, 46, 99, 111, 109, 47, 105, 110, 98, 111, 120, 47);
	boolean redoLastAccount = false;

	/**
	 * Creates and/or registers RuneScape accounts
	 * 
	 * @param create
	 *            - true if accounts are to be created; else, false
	 * @param register
	 *            - true if accounts are to be registered; else, false
	 * @param baseEmailName
	 *            - the email name without numbers attached to the end (excluding domain)
	 * @param emailDomain
	 *            - the domain of the email addresses
	 * @param password
	 *            - the password of the created RuneScape accounts
	 * @param firstExtension
	 *            - the first number extension to attach to the end of baseEmailName (-1 for no extension)
	 * @param lastExtension
	 *            - the last number extension to attach to the end of baseEmailName
	 * @param logDirectory
	 *            - the directory of the log file
	 * @throws IllegalArgumentException
	 *             when both <code>create</code> and <code>register</code> are false
	 */
	public void performTask(boolean create, boolean register, String baseEmailName, String emailDomain,
			String password, int firstExtension, int lastExtension, String logDirectory) throws Exception {
		if (!(create || register)) {
			throw new IllegalArgumentException("One of \"create\" or \"register\" must be true");
		}

		log = new Logger(logDirectory);
		String baseEmail = baseEmailName + "@" + emailDomain;

		long timeStarted = -1;

		try {
			setUp();

			timeStarted = System.nanoTime();

			logIntroSummary(getJobType(create, register), baseEmail, password, firstExtension, lastExtension);

			currentCreationUrl = CREATION_URL;
			int accountsManaged = 0;
			int accountsToMake = lastExtension - firstExtension + 1;

			String email = "";
			String previousEmail = "";

			for (; accountsManaged < accountsToMake; accountsManaged++) {
				previousEmail = email;

				String extension = (firstExtension + accountsManaged) == -1 ? "" : (accountsManaged + firstExtension)
						+ "";
				email = baseEmailName + extension + "@" + emailDomain;

				if (create) {
					while (!createAccount(currentCreationUrl, email, password, true)) {
						currentCreationUrl = getNewAccountCreationUrl(currentCreationUrl);

						if (redoLastAccount) {
							createAccount(currentCreationUrl, previousEmail, password, false);
						}
					}

					String viaProxy = currentCreationUrl.equals(CREATION_URL) ? "" : " (via proxy server)";
					if (!log.read().contains(email) || (email.equals(baseEmail) && log.occurencesOf(email) <= 1)) {
						if (register) {
							log.print(email + ": created" + viaProxy);
						} else {
							log.println(email + ": created" + viaProxy);
						}
					}
				}

				if (register) {
					registerEmail(baseEmailName + extension, emailDomain, password, create);

					if (create) {
						log.println(" and registered");
					} else {
						log.println(email + ": registered");
					}
				}

				redoLastAccount = false;
			}

			long timeStopped = System.nanoTime();
			String millisElapsed = toDurationString((timeStopped - timeStarted) / 1000000);
			log.println();

			String mainTask = create ? " created" : " registered";
			String registeredString = create && register ? "and registered " : "";

			switch (accountsManaged) {
			case 0:
				log.println("No accounts were" + mainTask + ".");
				break;
			case 1:
				log.println("1 account was" + mainTask + " " + registeredString + "in" + millisElapsed + ".");
				break;
			default:
				log.println(accountsManaged + " accounts were" + mainTask + " " + registeredString + "in"
						+ millisElapsed + ".");
				break;
			}

			tearDown();
		} catch (Exception e) {
			String logMessage = e.toString() + ": " + e.getMessage();
			if (e.toString().toLowerCase().contains("sessionnotfound")) {
				logMessage = errorMessage;
			}
			log.println();
			log.println();
			log.println("ERROR encountered at" + toDurationString((System.nanoTime() - timeStarted) / 1000000) + ":");
			log.println(logMessage);
			driver.quit();
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			errorMessage = "A problem was encountered. Details are in the log.";
		}
	}

	private String getNewAccountCreationUrl(String currentUrl) {
		String proxyUrl = "https://1.hidemyass.com/ip-1/encoded/czovL3NlY3VyZS5ydW5lc2NhcGUuY29tL209YWNjb3VudC1jcmVhdGlvbi9jcmVhdGVfYWNjb3VudA%3D%3D&f=norefer";
		boolean containsHide = currentUrl.toLowerCase().contains("hide");
		boolean containsRunescape = currentUrl.toLowerCase().contains("runescape");

		if (!(containsHide || containsRunescape)) {
			throw new IllegalArgumentException("Invalid URL for RuneScape account creation");
		}

		if (containsHide) {
			if (currentUrl.contains("ip-8")) {
				return CREATION_URL;
			} else {
				int ipIndex = currentUrl.indexOf("ip-") + 3;
				int newIp = Integer.parseInt(currentUrl.charAt(ipIndex) + "") + 1;
				return currentUrl.substring(0, ipIndex) + newIp + currentUrl.substring(ipIndex + 1);
			}
		} else {
			return proxyUrl;
		}
	}

	private String getJobType(boolean create, boolean register) {
		if (create) {
			if (register) {
				return "create and register";
			} else {
				return "create";
			}
		} else {
			return "register";
		}
	}

	private boolean createAccount(String url, String email, String password, boolean quitOnTakenEmail) {
		do {
			driver.get(url);
		} while (driver.getPageSource().contains("could not be loaded"));

		String source = driver.getPageSource().toLowerCase();

		if (source.contains("unable to create") || source.contains("error=1")) {
			if (source.contains("error=1")) {
				redoLastAccount = true;
			}
			return false;
		}

		driver.findElement(By.id("createEmail")).clear();
		driver.findElement(By.id("createEmail")).sendKeys(email);
		driver.findElement(By.id("createPassword")).clear();
		driver.findElement(By.id("createPassword")).sendKeys(password);
		driver.findElement(By.id("createAge")).clear();
		driver.findElement(By.id("createAge")).sendKeys(generateAge());
		driver.findElement(By.name("submit")).click();

		WebElement error = utilities.checkForElement(By.className("error"), 0, IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);

		if (error != null && quitOnTakenEmail) {
			errorMessage = "Account creation error: " + error;
			driver.quit();
		}

		if (url.toLowerCase().contains("hide")) {
			utilities.waitUntilUrlDoesntContain("=norefer");

			if (driver.getPageSource().contains("error=1")) {
				return false;
			} else if (driver.getPageSource().contains("could not be loaded")) {
				return false;
			}
		} else {
			if (utilities.waitUntilUrlContains("account_created", "error").contains("error")) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Registers an RuneScape account via email confirmation.
	 * 
	 * @param emailName
	 *            - the RuneScape account's email address (excluding domain)
	 * @param emailDomain
	 *            - the domain for the above email name
	 * @param password
	 *            - the password of the RuneScape account
	 * @param createOnFail
	 *            - true if the specified account should be created in the case that it does not exist; else, false
	 */
	private void registerEmail(String emailName, String emailDomain, String password, boolean createOnFail) {
		String loginEmail = emailName + "@" + emailDomain;
		String newEmailName = BrowserUtilities.getRandomString();
		String newEmailDomain = build(103, 114, 114, 46, 108, 97);
		String newEmail = newEmailName + "@" + newEmailDomain;

		createEmailInbox(newEmailName, newEmailDomain);

		logIntoRunescape(loginEmail, password, createOnFail);

		String url = driver.getCurrentUrl();
		String sessionCode = url.substring(url.indexOf("c=") + 2, url.indexOf("/account"));

		changeRunescapeEmail(newEmail, sessionCode);

		receiveRegistrationEmail();
	}

	/**
	 * Creates an email inbox on a disposable email website.
	 * 
	 * @param emailName
	 *            - the name of the inbox's email address (excluding domain)
	 * @param emailDomain
	 *            - the domain for the above email name
	 */
	private void createEmailInbox(String emailName, String emailDomain) {
		if (driver.getWindowHandles().size() == 1) {
			utilities.openNewWindow(EMAIL_WEBSITE);
		} else {
			utilities.switchWindow();
			driver.get(EMAIL_WEBSITE);
		}

		new Select(driver.findElement(By.id("gm-host-select"))).selectByVisibleText(emailDomain);
		driver.findElement(By.id("inbox-id")).click();
		driver.findElement(By.cssSelector("input[type=\"text\"]")).click();
		driver.findElement(By.cssSelector("input[type=\"text\"]")).clear();
		driver.findElement(By.cssSelector("input[type=\"text\"]")).sendKeys(emailName);
		driver.findElement(By.xpath("//span[@id='inbox-id']/button")).click();

		utilities.switchWindow();
	}

	/**
	 * Logs into the RuneScape website using an account's credentials.
	 * 
	 * @param email
	 *            - the full email address of the RuneScape account
	 * @param password
	 *            - the password of the RuneScape account
	 * @param createOnFail
	 *            - true if the specified account should be created in the case that it does not exist; else, false
	 */
	private void logIntoRunescape(String email, String password, boolean createOnFail) {
		driver.get("https://www.runescape.com/account_settings.ws");

		if (utilities.checkForElement(By.id("recaptcha_box"), 0, IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS) != null) {
			errorMessage = "Captcha encountered. Unfortunately, this program's not that smart.";
			driver.quit();
		}

		if (driver.findElement(By.id("remember")).isSelected()) {
			utilities.retryingFindClick(By.id("remember"));
		}

		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys(email);
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.name("submit")).click();

		WebElement error = utilities.checkForElement(By.className("errorMessage"), 0, IMPLICIT_WAIT_SECONDS,
				TimeUnit.SECONDS);

		if (error != null) {
			if (createOnFail) {
				createAccount(getNewAccountCreationUrl(currentCreationUrl), email, password, true);
				logIntoRunescape(email, password, false);
			} else {
				errorMessage = "Login error: " + error.getText();
				driver.quit();
			}
		}

		utilities.waitUntilUrlContains("c=");
	}

	/**
	 * Changes a RuneScape account's default email address. Note that this does not change the login email address.
	 * 
	 * @param newEmail
	 *            - the new, full email address of the RuneScape account
	 * @param sessionCode
	 *            - a code corresponding to the login session
	 */
	private void changeRunescapeEmail(String newEmail, String sessionCode) {
		driver.get("https://secure.runescape.com/m=email-register/c=" + sessionCode
				+ "/cancel_action.ws?type=address&IFrame=1");

		utilities.waitUntilUrlContains("mod");

		utilities.sendKeys(Keys.END);

		By boxClosed = By.xpath("//div[@id='email' and @class='RaggedBox TwoThirds RaggedBoxToggly RaggedBoxTogglyJs RaggedBoxClosed']");

		By raggedLocator = By.cssSelector("#email > div.RaggedBoxHeader > h3.RaggedBoxTitle.HoverText");

		new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(raggedLocator));

		driver.findElement(raggedLocator).click();

		utilities.sendKeys(Keys.END);
		
		driver.switchTo().frame("email_registration");

		By field1Locator = By.id("na");
		By field2Locator = By.id("na2");
		new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(field1Locator));
		driver.findElement(field1Locator).clear();
		driver.findElement(field1Locator).sendKeys(newEmail);
		driver.findElement(field2Locator).clear();
		driver.findElement(field2Locator).sendKeys(newEmail);
		driver.findElementByCssSelector("#agree_privacy > span").click();
		driver.findElementByName("submit").click();

		if(utilities.checkForElement(boxClosed, 0, IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS) != null) {
			driver.findElement(raggedLocator).click();
			driver.findElementByName("submit").click();
		}

		driver.switchTo().defaultContent();
	}

	/**
	 * Receives an email from RuneScape.com and uses the provided link to register the RuneScape account.
	 */
	private void receiveRegistrationEmail() {
		utilities.switchWindow();

		By emailLocator = By.cssSelector("td.td3");

		new WebDriverWait(driver, 60).until(ExpectedConditions.textToBePresentInElementLocated(emailLocator,
				"Confirm your"));
		driver.findElementByCssSelector("td.td3").click();

		By linkLocator = By.cssSelector("a[href*='submit_code']");
		new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(linkLocator));
		driver.get(driver.findElement(linkLocator).getText());
		utilities.waitUntilUrlContains("submit_code");
	}

	/**
	 * Sets up the FirefoxDriver and other essential objects.
	 * 
	 * @throws Exception
	 */
	private void setUp() throws Exception {
		FirefoxProfile fp = new FirefoxProfile();
		fp.setPreference("webdriver.load.strategy", "unstable");
		driver = new FirefoxDriver(fp);
		utilities = new BrowserUtilities(driver);
		driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
	}

	/**
	 * Quits the driver.
	 * 
	 * @throws Exception
	 */
	private void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	private static String toDurationString(long milliseconds) {
		return String
				.format(" %01d hours, %01d minutes, and %01d seconds",
						TimeUnit.MILLISECONDS.toHours(milliseconds),
						TimeUnit.MILLISECONDS.toMinutes(milliseconds)
								- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
						TimeUnit.MILLISECONDS.toSeconds(milliseconds)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
				.replace(" 1 hours", " 1 hour").replace(" 1 minutes", " 1 minute").replace(" 1 seconds", " 1 second");
	}

	private static String generateAge() {
		return Integer.toString(16 + (int) (Math.random() * ((24 - 16) + 1)));
	}

	private static String build(int... numbers) {
		String string = "";

		for (int number : numbers) {
			string += "" + (char) number;
		}

		return string;
	}

	private void logIntroSummary(String jobType, String baseEmail, String password, int firstExtension,
			int lastExtension) {
		log.println("RuneScape Account Generator log for " + Calendar.getInstance().getTime());
		log.println();

		int accountsToMake = lastExtension - firstExtension + 1;
		String accountPlural = accountsToMake == 1 ? "account" : "accounts";
		String emailBaseName = "\"" + baseEmail + "\"";
		String passwordString = "\"" + password + "\"";
		String emailPasswordString = " with the email base name " + emailBaseName + ", password " + passwordString
				+ ", ";

		if (accountsToMake == 1 && firstExtension == -1) {
			log.println("Attempting to " + jobType + " 1 account" + emailPasswordString + "and no number extensions.");
		} else {
			log.println("Attempting to " + jobType + " " + accountsToMake + " " + accountPlural + emailPasswordString
					+ "and extensions " + (firstExtension == -1 ? "NONE" : firstExtension) + " to " + lastExtension
					+ ".");
		}

		log.println();
	}
}