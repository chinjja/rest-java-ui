package com.chinjja.rest;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CreateDialog extends JDialog {
	private final Map<String, JTextField> inputs = new HashMap<>();
	private final JPanel buttons = new JPanel();
	private final JPanel properties = new JPanel();
	private final JButton create = new JButton("Create");
	private final JButton cancel = new JButton("Cancel");
	public CreateDialog(App app) {
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		getContentPane().add(buttons, BorderLayout.SOUTH);
		create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JsonObject body = new JsonObject();
				for(Entry<String, JsonElement> entry : app.properties.entrySet()) {
					body.addProperty(entry.getKey(), inputs.get(entry.getKey()).getText());
				}
				app.create(body).subscribe();
				dispose();
			}
		});
		
		buttons.add(create);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		buttons.add(cancel);
		
		getContentPane().add(properties, BorderLayout.CENTER);
		properties.setLayout(new GridLayout(0, 1, 0, 0));
		
		for(Entry<String, JsonElement> entry : app.properties.entrySet()) {
			JsonObject obj = entry.getValue().getAsJsonObject();
			JLabel title = new JLabel(obj.get("title").getAsString());
			JTextField field = new JTextField();
			field.setEnabled(!obj.get("readOnly").getAsBoolean());
			
			JPanel row = new JPanel(new BorderLayout());
			row.add(BorderLayout.WEST, title);
			row.add(BorderLayout.CENTER, field);
			
			inputs.put(entry.getKey(), field);
			properties.add(row);
		}
	}

}
