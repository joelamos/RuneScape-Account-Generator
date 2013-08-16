package com.joelchristophel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.sun.jmx.snmp.Timestamp;

public class AccountGenerator {
	private WebDriver driver;
	private String baseUrl;
	private static GUI gui = new GUI();
	private PrintWriter out;
	private StringBuffer verificationErrors = new StringBuffer();

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				gui.setVisible(true);
			}
		});
	}
	public AccountGenerator() {
		try {
			File file = new File(gui.getPath() + "rsag_log_"
					+ new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()) + ".txt");
			file.getParentFile().mkdirs();
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void generateAccounts(String baseEmailName, String emailDomain, String password, int firstExtension,
			int lastExtension) throws Exception {
		setUp();
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		out.println("RuneScape Account Generator log for " + new Date(stamp.getDateTime()));
		out.println();
		out.println("Attempting to create " + (lastExtension - firstExtension + 1)
				+ " accounts with the email base name \"" + baseEmailName + "@" + emailDomain + "\", password \""
				+ password + "\", and extensions " + firstExtension + " to " + lastExtension + ".");
		out.println();

		int i = firstExtension;
		int accountsMade = 0;
		
		long timeStarted = System.nanoTime();

		for (; i <= lastExtension; i++) {
			String email = baseEmailName + (i) + "@" + emailDomain;
			driver.get(baseUrl);
			driver.findElement(By.id("age")).sendKeys(generateAge());
			driver.findElement(By.id("email1")).sendKeys(email);
			driver.findElement(By.id("email2")).sendKeys(email);
			driver.findElement(By.id("password1")).sendKeys(password);
			driver.findElement(By.name("submit")).click();

			while (!driver.getCurrentUrl().contains("runescape.com/game.ws")) {
				if (driver.getCurrentUrl().contains("denied_not_eligible")) {
					out.println("Aborting: no longer allowed to make accounts.");
					return;
				}

				Thread.sleep(1000);
			}

			out.println(email);
			accountsMade++;
		}

		long timeStopped = System.nanoTime();
		String millisElapsed = toDurationString((timeStopped - timeStarted) / 1000000);
		out.println();

		switch (accountsMade) {
		case 0:
			out.println("No accounts were created.");
			break;
		case 1:
			out.println("1 account was created in" + millisElapsed + ".");
			break;
		default:
			out.println(accountsMade + " accounts were created in" + millisElapsed + ".");
			break;
		}

		tearDown();
	}

	public PrintWriter getPrintWriter() {
		return out;
	}

	private void setUp() throws Exception {
		FirefoxProfile fp = new FirefoxProfile();
		fp.setPreference("webdriver.load.strategy", "unstable");
		driver = new FirefoxDriver(fp);
		baseUrl = "https://secure.runescape.com/m=account-creation/";
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
	}

	private void tearDown() throws Exception {
		out.close();
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
}