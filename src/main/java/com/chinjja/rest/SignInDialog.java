package com.chinjja.rest;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SignInDialog extends JDialog {
	private final JPanel panel = new JPanel();
	private final JLabel lblNewLabel = new JLabel("Username");
	private final JLabel lblNewLabel_1 = new JLabel("Password");
	private final JTextField username = new JTextField();
	private final JPasswordField password = new JPasswordField();
	private final JButton signIn = new JButton("Sign In");
	public SignInDialog(App app) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		username.setColumns(10);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		GridBagConstraints gbc_username = new GridBagConstraints();
		gbc_username.fill = GridBagConstraints.HORIZONTAL;
		gbc_username.gridx = 2;
		gbc_username.gridy = 0;
		panel.add(username, gbc_username);
		
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		GridBagConstraints gbc_password = new GridBagConstraints();
		gbc_password.fill = GridBagConstraints.HORIZONTAL;
		gbc_password.gridx = 2;
		gbc_password.gridy = 1;
		panel.add(password, gbc_password);
		
		GridBagConstraints gbc_signIn = new GridBagConstraints();
		gbc_signIn.fill = GridBagConstraints.HORIZONTAL;
		gbc_signIn.gridwidth = 2;
		gbc_signIn.gridx = 1;
		gbc_signIn.gridy = 2;
		signIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(app.signIn(username.getText(), password.getPassword())) {
					dispose();
				} else {
					JOptionPane.showMessageDialog(getRootPane(), "cannot sign-in");
					username.requestFocus();
					username.selectAll();
				}
			}
		});
		panel.add(signIn, gbc_signIn);
		
		getRootPane().setDefaultButton(signIn);
	}

}
