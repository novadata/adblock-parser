package com.novacloud.data.adblock.parser;

import java.util.regex.Pattern;

public enum Regex {
	SEPARATOR("[^A-Za-z0-9_%.-]")
	, WILDCARD(".*")
	, HTTP_WILDCARD("^https?:\\/\\/")
	, STARTS("^")
	, ENDS("$")

	, SPLIT_RULE("[" + Pattern.quote(Terminal.SEPARATOR.value()) + Pattern.quote(Terminal.WILDCARD.value()) + "]")
	;

	private final String value;

	private Regex(final String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}
