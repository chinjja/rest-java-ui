package com.chinjja.rest;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EmployeeTable extends JPanel {
	App app;
	private final JPanel panel = new JPanel();
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTable table = new JTable();
	private final JButton first = new JButton("<<");
	private final JButton prev = new JButton("<");
	private final JButton next = new JButton(">");
	private final JButton last = new JButton(">>");
	public EmployeeTable(App app) {
		this.app = app;
		setLayout(new BorderLayout(0, 0));
		
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 4, 0, 0));
		first.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				app.loadFromServer(e.getActionCommand()).subscribe();
			}
		});
		
		panel.add(first);
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				app.loadFromServer(e.getActionCommand()).subscribe();
			}
		});
		
		panel.add(prev);
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				app.loadFromServer(e.getActionCommand()).subscribe();
			}
		});
		
		panel.add(next);
		last.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				app.loadFromServer(e.getActionCommand()).subscribe();
			}
		});
		
		panel.add(last);
		
		add(scrollPane, BorderLayout.CENTER);
		table.setShowVerticalLines(true);
		table.setShowHorizontalLines(true);
		
		scrollPane.setViewportView(table);
	}

	public void update(JsonObject obj) {
		JsonElement embedded = obj.get("_embedded");
		JsonArray employees = embedded.getAsJsonObject().get("employees").getAsJsonArray();
		DefaultTableModel model = new DefaultTableModel(new Vector<>(Arrays.asList("First Name", "Last Name", "Description")), app.getPageSize());
		for(int i = 0; i < employees.size(); i++) {
			JsonObject employee = employees.get(i).getAsJsonObject();
			model.setValueAt(employee.get("firstName").getAsString(), i, 0);
			model.setValueAt(employee.get("lastName").getAsString(), i, 1);
			model.setValueAt(employee.get("description").getAsString(), i, 2);
		}
		table.setModel(model);
		
		JsonObject links = (JsonObject)obj.get("_links");
		HashMap<String, JButton> map = new HashMap<>();
		map.put("first", first);
		map.put("prev", prev);
		map.put("next", next);
		map.put("last", last);
		for(Map.Entry<String, JButton> e : map.entrySet()) {
			String key = e.getKey();
			JButton btn = e.getValue();
			if(links.has(key)) {
				btn.setEnabled(true);
				btn.setActionCommand(links.get(key).getAsJsonObject().get("href").getAsString());
			} else {
				btn.setEnabled(false);
			}
		}
		revalidate();
		repaint();
	}
}
