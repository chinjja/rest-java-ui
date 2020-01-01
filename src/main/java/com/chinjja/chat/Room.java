package com.chinjja.chat;

public interface Room {
	String getId();
	String getName();
	void setName(String name);
	User[] getUsers();
	void addUsers(User...users);
	void removeUsers(User...users);
	User getOwner();
	void setOwner(User owner);
}
