package com.chinjja.chat;

public interface User {
	String getId();
	String getName();
	void setName(String name);
	User[] getFriends();
	void addFriends(User...friends);
	void removeFriends(User...friends);
	Room[] getMyRooms();
}
