package com.spike.h2o_app;

public class User {
	
	String username;
	String password;
	Boolean admin;
	
	public User(String username, String password, Boolean admin) {
		this.username = username;
		this.password = password;
		this.admin = admin;
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}	
	
	public Boolean isAdmin() {
		return this.admin == true ? true : false;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
}
