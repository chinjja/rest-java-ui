package com.chinjja.rest;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.gson.JsonObject;

public class CreateDialog extends JDialog {
	private final JLabel lblNewLabel = new JLabel("First Name");
	private final JTextField firstName = new JTextField();
	private final JLabel lblNewLabel_1 = new JLabel("Last Name");
	private final JTextField lastName = new JTextField();
	private final JLabel lblNewLabel_2 = new JLabel("Description");
	private final JTextField description = new JTextField();
	private final JPanel panel = new JPanel();
	private final JButton btnNewButton = new JButton("Create");
	private final JButton btnNewButton_1 = new JButton("Cancel");
	public CreateDialog(App app) {
		description.setColumns(10);
		lastName.setColumns(10);
		firstName.setColumns(10);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		getContentPane().add(lblNewLabel, gbc_lblNewLabel);
		
		GridBagConstraints gbc_firstName = new GridBagConstraints();
		gbc_firstName.gridx = 1;
		gbc_firstName.gridy = 0;
		getContentPane().add(firstName, gbc_firstName);
		
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		getContentPane().add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		GridBagConstraints gbc_lastName = new GridBagConstraints();
		gbc_lastName.gridx = 1;
		gbc_lastName.gridy = 1;
		getContentPane().add(lastName, gbc_lastName);
		
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		getContentPane().add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		GridBagConstraints gbc_description = new GridBagConstraints();
		gbc_description.gridx = 1;
		gbc_description.gridy = 2;
		getContentPane().add(description, gbc_description);
		
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridwidth = 2;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		getContentPane().add(panel, gbc_panel);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JsonObject obj = new JsonObject();
				obj.addProperty("firstName", firstName.getText());
				obj.addProperty("lastName", lastName.getText());
				obj.addProperty("description", description.getText());
				app.create(obj).subscribe();
				dispose();
			}
		});
		
		panel.add(btnNewButton);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		panel.add(btnNewButton_1);
	}

}
