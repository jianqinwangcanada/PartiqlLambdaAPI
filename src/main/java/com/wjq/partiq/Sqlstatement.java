package com.wjq.partiq;

import com.google.gson.Gson;

public class Sqlstatement {
	
	private String insertSQL;
//	public Sqlstatement(String insertSQL) {
//		super();
//		this.insertSQL = insertSQL;
//	}
	public Sqlstatement(String json) {
		Gson gson = new Gson();
		Sqlstatement temp = gson.fromJson(json, Sqlstatement.class);
		this.insertSQL= temp.insertSQL;
		
	}

	public String getInsertSQL() {
		return insertSQL;
	}
	public void setInsertSQL(String insertSQL) {
		this.insertSQL = insertSQL;
	}

	public String toString() {
		return new Gson().toJson(this);
	}
	
}
