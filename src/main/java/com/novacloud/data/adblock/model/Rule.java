package com.novacloud.data.adblock.model;

import java.io.Serializable;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Rule implements Serializable {

	public static Rule getEmptyRule() {
		return new Rule(RuleType.empty, null, null, null,false,null);
	}

	private final RuleType type;
	private final Pattern urlRegex;
	private final String effLine;
	private final String line;
	String domains="*";
	String selector=null;
	final String rawOpts;
	final boolean containsScriptOpt;
	public Rule(final RuleType type, final Pattern urlRegex, final String effLine, final String line
	,final boolean containsScriptOpt,final String rawOpts) {
		this.type = type;
		this.urlRegex = urlRegex;
		this.effLine = effLine;
		this.line = line;
		this.containsScriptOpt=containsScriptOpt;
		this.rawOpts=rawOpts;
	}

	@Override
	public String toString() {
		return String.format("type:%s,urlRegex:%s, line:%s, effline:%s, rawOpts:%s",type, urlRegex,line,effLine,rawOpts);
	}

	public boolean applies(final String url) {
		return urlRegex != null && urlRegex.matcher(url).matches();
	}

	public Pattern getUrlRegex() {
		return urlRegex;
	}

	public String getEffectiveLine() {
		return effLine;
	}

	public String getLine() {
		return line;
	}

	public RuleType getType() {
		return type;
	}
	public boolean isContainsScriptOpt() {
		return containsScriptOpt;
	}



	public String getRawOpts() {
		return rawOpts;
	}


	public String getEffLine() {
		return effLine;
	}
	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getDomains() {
		return domains;
	}
	public String[] getDomainList() {
		return domains.split(",");
	}
	public boolean containDomain(String domain){
		for (String s : getDomainList()) {
			if(s.equalsIgnoreCase(domain)) {
				return true;
			}
		}
		return  false;
	}
	public boolean isForAllDomain(){
		return (domains == null || "*".equals(domains) || domains.length()==0);
	}
	public void setDomains(String domains) {
		this.domains = domains;
	}
}
