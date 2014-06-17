package com.joelchristophel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private String logPath;

	public Logger(String directory) {
		makeLogFile(directory);
	}

	public void print(String text) {
		File logFile = new File(logPath);
		String contents = read();

		try (PrintWriter out = new PrintWriter(logFile)) {
			out.print(contents + text);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void println(String text) {
		File logFile = new File(logPath);
		String contents = read();

		try (PrintWriter out = new PrintWriter(logFile)) {
			out.println(contents + text);
			out.println();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void println() {
		println("");
	}

	public String read() {
		String text = "";

		try (BufferedReader reader = new BufferedReader(new FileReader(new File(logPath)))) {
			String line = null;

			while ((line = reader.readLine()) != null) {
				text += String.format(line + "%n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (text.equals("")) {
			return text;
		}

		return text.substring(0, text.length() - String.format("%n").length());
	}

	private void makeLogFile(String directory) {
		String name = "rsag_log_" + new SimpleDateFormat("MM-dd-yyyy_hh-mm-ss").format(new Date()) + ".txt";
		logPath = directory + File.separator + name;

		try {
			File file = new File(logPath);
			file.getParentFile().mkdirs();
			PrintWriter out = new PrintWriter(file);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int occurencesOf(String phrase) {
		String text = read();
		return (text.length() - text.replace(phrase, "").length()) / phrase.length();
	}
}