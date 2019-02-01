package com.longlong.exporter.exception;

public class ExportException extends RuntimeException {

	private static final long serialVersionUID = 4615422570580250697L;

	public ExportException(String message) {
		super(message);
	}

	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}
}
