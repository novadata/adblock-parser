package com.novacloud.data.adblock.parser;

@SuppressWarnings("serial")
public class ParserException extends Exception {

	public ParserException(final String message, final Throwable t) {
		super(message, t);
	}
}
