package com.longlong.excel.exceptions;

public class ImportException extends RuntimeException {
	private static final long serialVersionUID = 4615422570580250698L;

	public ImportException(String message) {
		super(message);
	}

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}
}
