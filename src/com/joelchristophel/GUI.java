package com.joelchristophel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

class GUI extends JFrame {
	private GridBagLayout gridBag = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();
	private Color color = new Color(0xF1F5FB);
	private JFileChooser chooser;
	private String directoryPath = "";
	private boolean pathEmpty = true;
	private boolean passwordEmpty = true;
	private boolean numberOfAccountsEmpty = true;
	private boolean emailEmpty;

	GUI() {
		setProperties();
		addComponents();

		for (Component component : getComponents(this)) {
			if (component instanceof JPanel) {
				component.setBackground(color);
			}
		}
	}

	public String getPath() {
		return directoryPath.endsWith(File.separator) ? directoryPath : directoryPath + File.separator;
	}

	private GUI getThis() {
		return this;
	}

	private void setProperties() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		chooser = new JFileChooser();

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
				}
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e2) {
				e2.printStackTrace();
			}
		}

		setLayout(gridBag);
		getContentPane().setBackground(color);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(400, 500));
		setResizable(false);
		setTitle("RuneScape Account Generator");
		setLocationRelativeTo(null);
	}

	private void addComponents() {
		Font font = new Font("Arial", Font.BOLD, 12);
		JPanel emailPanel = new JPanel(gridBag);
		JPanel browsePanel = new JPanel(gridBag);
		JPanel extensionPanel = new JPanel(gridBag);
		JLabel emailLabel = new JLabel("Base email");
		emailLabel.setFont(font);
		JLabel title = new JLabel("RuneScape Account Generator");
		title.setFont(new Font("Arial", Font.BOLD, 18));
		JLabel author = new JLabel("by joelamos");
		author.setFont(new Font("Arial", Font.BOLD, 13));
		JLabel atLabel = new JLabel("@");
		JLabel toLabel = new JLabel("to");
		Font plain14 = new Font("Arial", Font.PLAIN, 14);
		atLabel.setFont(plain14);
		toLabel.setFont(plain14);
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setFont(font);
		JLabel numberExtensionsLabel = new JLabel("Number extensions");
		numberExtensionsLabel.setFont(font);
		JLabel logLabel = new JLabel("Save log to");
		logLabel.setFont(font);
		final JButton letsDoThis = new JButton("Let's do this!");
		letsDoThis.setFont(new Font("Dialog", Font.BOLD, 12));
		letsDoThis.setEnabled(false);
		JButton browse = new JButton("Browse");
		final JTextField email = new JTextField(11);
		final JTextField extensionStart = new JTextField(4);
		final JTextField extensionFinish = new JTextField(4);
		final JTextField path = new JTextField(16);
		path.setEditable(false);
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooser.setBackground(new Color(0xFFFF));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				directoryPath = chooser.showOpenDialog(getThis()) == JFileChooser.APPROVE_OPTION ? chooser
						.getSelectedFile().getAbsolutePath() : directoryPath;
				path.setText(directoryPath);
			}
		});
		final JPasswordField password = new JPasswordField(16);
		final JComboBox<String> domains = new JComboBox<String>(new String[] { "yahoo.com", "gmail.com", "aol.com",
				"hotmail.com", "msn.com", "comcast.net", "verizon.net" });
		DocumentListener textFieldListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				actionPerformed(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				actionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				actionPerformed(e);
			}

			public void actionPerformed(DocumentEvent e) {
				Document document = e.getDocument();

				if (document.equals(extensionStart.getDocument())) {
					if (extensionStart.getText() == null || extensionStart.getText().equals("")) {
						numberOfAccountsEmpty = true;
					} else {
						numberOfAccountsEmpty = false;
					}
				} else if (document.equals(path.getDocument())) {
					if (path.getText() == null || path.getText().equals("")) {
						pathEmpty = true;
					} else {
						pathEmpty = false;
					}
				} else if (document.equals(password.getDocument())) {
					if (password.getPassword() == null || password.getPassword().length == 0) {
						passwordEmpty = true;
					} else {
						passwordEmpty = false;
					}
				} else if (document.equals(email.getDocument())) {
					if (email.getText() == null || email.getText().equals("")) {
						emailEmpty = true;
					} else {
						emailEmpty = false;
					}
				}

				letsDoThis.setEnabled((numberOfAccountsEmpty || pathEmpty || passwordEmpty || emailEmpty) ? false
						: true);
			}
		};

		email.getDocument().addDocumentListener(textFieldListener);
		extensionStart.getDocument().addDocumentListener(textFieldListener);
		path.getDocument().addDocumentListener(textFieldListener);
		password.getDocument().addDocumentListener(textFieldListener);

		letsDoThis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				String error = null;

				if (email.getText().length() > 58) {
					error = "The maximum email length is 58 characters, not including the domain.";
				} else if (email.getText().length() + extensionStart.getText().length() > 58) {
					error = "Adding the specified numbers to the end of this email address will cause it to exceed the maximum character length of 58.";
				} else if (!email.getText().matches("[a-zA-Z0-9-_]+")) {
					error = "Email addresses may only contain alphanumeric characters, dashes, and underscores.";
				} else if (password.getPassword().length < 5 || password.getPassword().length > 25) {
					error = "Passwords must be 5-25 characters in length.";
				} else if (!String.valueOf(password.getPassword()).matches("[a-zA-Z0-9]+")) {
					error = "Passwords may only contain alphanumeric characters.";
				} else if (!passwordIsAllowed(String.valueOf(password.getPassword()))) {
					error = "Jagex has blocked the password you have provided. Pick a harder password.";
				} else if (!extensionStart.getText().matches("[0-9-]+") || (!extensionFinish.getText().equals(""))
						&& !extensionFinish.getText().matches("[0-9]+")) {
					error = "\"Number extensions\" implies...numbers.";
				} else if (extensionStart.getText().length() > 4) {
					error = "Keep your number extensions within 4 digits.";
				} else if (Integer.parseInt(extensionStart.getText()) < 0
						|| (!extensionFinish.getText().equals("") && Integer.parseInt(extensionFinish.getText()) < 0)) {
					error = "Number extensions should be positive";
				} else if (!extensionFinish.getText().equals("")
						&& (Integer.parseInt(extensionFinish.getText()) < Integer.parseInt(extensionStart.getText()))) {
					error = "The second number extension must be greater than the first.";
				} else if (!extensionFinish.getText().equals("")
						&& (Integer.parseInt(extensionFinish.getText()) - Integer.parseInt(extensionStart.getText())
								+ 1 > 100)) {
					error = "You may only attempt to create up to 100 acounts at a time.";
				} else if (!new File(path.getText()).exists()) {
					error = "The specified path does not exist.";
				}

				if (error == null) {
					try {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								JButton source = (JButton) e.getSource();
								source.setEnabled(false);
								source.setText("Working...");
							}
						});

						new SwingWorker<Void, Void>() {
							@Override
							protected Void doInBackground() throws Exception {
								AccountGenerator am = null;

								try {
									am = new AccountGenerator();
									am.generateAccounts(
											email.getText(),
											(String) domains.getSelectedItem(),
											String.valueOf(password.getPassword()),
											Integer.parseInt(extensionStart.getText()),
											extensionFinish.getText().equals("") ? Integer.parseInt(extensionStart
													.getText()) : Integer.parseInt(extensionFinish.getText()));

								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									if (am != null) {
										am.getPrintWriter().close();
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												JButton source = (JButton) e.getSource();
												source.setEnabled(true);
												source.setText("Let's do this!");
											}
										});
									}
								}
								return null;
							}
						}.execute();
					} catch (Exception e1) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								JButton source = (JButton) e.getSource();
								source.setEnabled(true);
								source.setText("Let's do this!");
							}
						});
						e1.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(getThis(), error);
				}
			}
		});

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.insets = new Insets(0, 0, 0, 8);
		gridBag.setConstraints(email, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.insets = new Insets(0, 0, 0, 8);
		gridBag.setConstraints(atLabel, constraints);

		constraints.gridx = 2;
		constraints.gridy = 1;
		gridBag.setConstraints(domains, constraints);

		emailPanel.add(email);
		emailPanel.add(atLabel);
		emailPanel.add(domains);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 0, 20);
		gridBag.setConstraints(path, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		gridBag.setConstraints(browse, constraints);

		browsePanel.add(path);
		browsePanel.add(browse);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 0, 8);
		gridBag.setConstraints(extensionStart, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 0, 8);
		gridBag.setConstraints(toLabel, constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 0, 8);
		gridBag.setConstraints(extensionFinish, constraints);

		extensionPanel.add(extensionStart);
		extensionPanel.add(toLabel);
		extensionPanel.add(extensionFinish);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 5, 0);
		gridBag.setConstraints(title, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.insets = new Insets(0, 0, 20, 0);
		gridBag.setConstraints(author, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.insets = new Insets(0, 0, 10, 300);
		gridBag.setConstraints(emailLabel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.insets = new Insets(0, 0, 20, 30);
		gridBag.setConstraints(emailPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.insets = new Insets(0, 0, 10, 305);
		gridBag.setConstraints(passwordLabel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.insets = new Insets(0, 0, 20, 115);
		gridBag.setConstraints(password, constraints);

		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.insets = constraints.insets = new Insets(0, 0, 10, 250);
		gridBag.setConstraints(numberExtensionsLabel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.insets = constraints.insets = new Insets(0, 0, 20, 150);
		gridBag.setConstraints(extensionPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 8;
		constraints.insets = constraints.insets = new Insets(0, 0, 10, 298);
		gridBag.setConstraints(logLabel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 9;
		constraints.insets = constraints.insets = new Insets(0, 0, 40, 2);
		gridBag.setConstraints(browsePanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 10;
		constraints.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(letsDoThis, constraints);

		add(title);
		add(author);
		add(emailLabel);
		add(emailPanel);
		add(passwordLabel);
		add(password);
		add(numberExtensionsLabel);
		add(extensionPanel);
		add(logLabel);
		add(browsePanel);
		add(letsDoThis);
	}

	private boolean passwordIsAllowed(String password) {
		password = password.toLowerCase();
		return password.equals("password") || password.equals("qwe123") || password.equals("123qwe")
				|| password.equals("246810") || password.equals("1357911") || "qwertyuiop".contains(password)
				|| "poiuytrewq".contains(password) || "asdfghjkl".contains(password) || "lkjhgfdsa".contains(password)
				|| "zxcvbnm".contains(password) || "mnbvcxz".contains(password) || "1234567890".contains(password)
				|| password.contains("12345678910") || password.matches("(.)\\1*") ? false : true;
	}

	private static Component[] getComponents(Component container) {
		ArrayList<Component> list = null;

		try {
			list = new ArrayList<Component>(Arrays.asList(((Container) container).getComponents()));
			for (int index = 0; index < list.size(); index++) {
				for (Component currentComponent : getComponents(list.get(index))) {
					list.add(currentComponent);
				}
			}
		} catch (ClassCastException e) {
			list = new ArrayList<Component>();
		}

		return list.toArray(new Component[list.size()]);
	}
}