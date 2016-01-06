package com.tmo.swagger.exception;

public class WrongXpath extends Exception {

	private static final long serialVersionUID = 1L;
	private String xpath = "";

	public WrongXpath(String x) {
		xpath = x;
	}

	@Override
	public String toString() {
		return ("Please find the wrong xpath ==" + xpath)
				.toString();
	}
}
