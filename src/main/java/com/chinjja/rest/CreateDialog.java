package com.chinjja.rest;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CreateDialog extends JDialog {
	private final Map<String, JTextField> inputs = new HashMap<>();
	private final JPanel buttons = new JPanel();
	private final JPanel properties = new JPanel();
	private final JButton create = new JButton("OK");
	private final JButton cancel = new JButton("Cancel");
	private final JPanel labels = new JPanel();
	public CreateDialog(App app, ResponseJson init) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		getContentPane().add(buttons, BorderLayout.SOUTH);
		create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JsonObject body = new JsonObject();
				for(Entry<String, JsonElement> entry : app.properties.entrySet()) {
					body.addProperty(entry.getKey(), inputs.get(entry.getKey()).getText());
				}
				if(init == null) {
					app.create(body).subscribe();
				} else {
					app.update(init, body).subscribe();
				}
				dispose();
			}
		});
		buttons.setLayout(new GridLayout(0, 2, 0, 0));
		
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
			JsonObject obj = App.object(entry.getValue());
			JLabel title = new JLabel(App.string(obj, "title"));
			JTextField field = new JTextField();
			field.setColumns(12);
			field.setEnabled(!App.bool(obj, "readOnly"));
			if(init != null) {
				field.setText(App.string(init.entity, entry.getKey()));
			}
			
			inputs.put(entry.getKey(), field);
			labels.add(title);
			properties.add(field);
		}
		
		getRootPane().setDefaultButton(create);
		getRootPane().registerKeyboardAction(e -> {
			dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);;
		
		getContentPane().add(labels, BorderLayout.WEST);
		labels.setLayout(new GridLayout(0, 1, 0, 0));
	}

}
