package org.util;

public class Fact {

	private int factId;
	private String factString;
	private Double factValue;


	Fact() {
	}

	Fact(int factId, String factString, Double factValue) {
		this.factId = factId;
		this.factString = factString;
		this.factValue = factValue;
	}

	public int getFactId() {
		return factId;
	}

	public void setFactId(int factId) {
		this.factId = factId;
	}

	public String getFactString() {
		return factString;
	}

	public void setFactString(String factString) {
		this.factString = factString;
	}

	public Double getFactValue() {
		return factValue;
	}

	public void setFactValue(double factValue) {
		this.factValue = factValue;
	}


}
