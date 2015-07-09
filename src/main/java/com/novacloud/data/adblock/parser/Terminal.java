package com.novacloud.data.adblock.parser;

public enum Terminal {
	EXCEPTION("@@")
	, HTTP_WILDCARD("||")
	, STARTS("|")
	, ENDS("|")
	, COMMENT("!")
	, SECTION("[")
	, SEPARATOR("^")
	, WILDCARD("*")
	, SELECTOR("##")
	, EXCEPT_SELECTOR("#@#")
	, NOT("~")
	, RULE_OPT("$")
	;

	private final String value;

	private Terminal(final String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public int length() {
		return value.length();
	}
}