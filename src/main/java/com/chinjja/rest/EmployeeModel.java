package com.chinjja.rest;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.google.gson.JsonObject;

public class EmployeeModel extends AbstractTableModel {
	public static final int FIRST_NAME = 0;
	public static final int LAST_NAME = 1;
	public static final int DESCRIPTION = 2;
	public static final int DELETE = 3;
	private static final int SIZE = 4;
	
	private final ArrayList<ResponseJson> employees = new ArrayList<>();
	
	@Override
	public int getRowCount() {
		return employees.size();
	}

	@Override
	public int getColumnCount() {
		return SIZE;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		JsonObject employee = employees.get(rowIndex).entity.getAsJsonObject();
		switch(columnIndex) {
		case FIRST_NAME: return employee.get("firstName").getAsString();
		case LAST_NAME: return employee.get("lastName").getAsString();
		case DESCRIPTION: return employee.get("description").getAsString();
		case DELETE: return employee;
		default: return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
		case FIRST_NAME: return "First name";
		case LAST_NAME: return "Last name";
		case DESCRIPTION: return "Description";
		case DELETE: return null;
		default: return super.getColumnName(column);
		}
	}

	public void clear() {
		employees.clear();
		fireTableDataChanged();
	}
	
	public void add(ResponseJson employee) {
		int idx = employees.size();
		employees.add(employee);
		fireTableRowsInserted(idx, idx);
	}
	
	public ResponseJson get(int idx) {
		return employees.get(idx);
	}
}
