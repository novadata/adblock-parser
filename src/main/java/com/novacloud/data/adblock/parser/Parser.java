package com.novacloud.data.adblock.parser;

import com.novacloud.data.adblock.model.Rule;
import com.novacloud.data.adblock.model.RuleType;

import java.util.regex.Pattern;

public class Parser {
	public Rule parse(final String line) throws ParserException {
		// get effective line for parsing
		String effLine;
		try {
			effLine = clean(line);
		} catch(final Exception e) {
			throw new ParserException("cleaning " + line, e);
		}
		if (effLine.isEmpty()) {
			return Rule.getEmptyRule();
		}


		//build selector
		String[] selector=buildSelector(effLine);
		// check if regular or exception
		final RuleType type;
		if (effLine.startsWith(Terminal.EXCEPTION.value())) {
			type = RuleType.exception;
			effLine = effLine.substring(Terminal.EXCEPTION.length());
		} else {
			type = RuleType.exclusion;
		}
		// transform effective line to regular expression
		Pattern regex;
		try {
			regex = buildUrlRegex(effLine);
		} catch(final Exception e) {
			throw new ParserException("building regex for " + effLine, e);
		}

		Rule rule = new Rule(type, regex, effLine, line,containsScriptOpt(line),getRawOpts(line));
		if(selector !=null && selector[0]!=null) {
			rule.setDomains(selector[0]);
		}
		if(selector !=null && selector[1]!=null) {
			rule.setSelector(selector[1]);
		}
		return rule;
	}

	private String clean(String line) {
		if (isComment(line)) {//is comment
			line = "";
		}
		line = cleanOptLine(line);

		if (line.contains(Terminal.EXCEPT_SELECTOR.value())) { // todo: div selector exception
			line = "";
		}

		return line.trim();
	}

	private String cleanSelectorLine(String line) {
		if (isHtmlRule(line)) {//only deal href= rule
			if (line.indexOf("a[href=\"http:", line.indexOf(Terminal.SELECTOR.value()))>0
						) {
				line = line.substring(line.indexOf("a[href=\"http:") + 8);
				line = line.substring(0, line.indexOf("\"]"));
			} else {
				line = "";
			}
		}
		return line;
	}

	private String cleanOptLine(String line) {
		if (containsOpt(line)) {
			if (isException(line)) {
				line = "";
			} else if(containsScriptOpt(line) && !containsDomainOpt(line)){//todo: simple deal script opt
				line = line.substring(0, line.indexOf(Terminal.RULE_OPT.value()));
			}
			else {//todo: just ignore other rule opt currently,should deal it
				line ="";
			}
		}
		return line;
	}

	/**
	 * only deal global element selector ,not deal domain-specific selector
	 * @param line  one line of rule file
	 * @return domain and selector
	 */
	private String[] buildSelector(final String line){
		if (!isComment(line)) {
			String[] domainSelector=new String[2];
			if (line.startsWith(Terminal.SELECTOR.value())) {
				domainSelector[0]="*";
				domainSelector[1]= line.substring(Terminal.SELECTOR.value().length());
			}else if(line.contains(Terminal.SELECTOR.value())){
				String[] split = line.split(Terminal.SELECTOR.value());
				if(split.length>1) {
					domainSelector[0] = split[0];
				}
				domainSelector[1]=split[1];
			}
			return domainSelector;
		}
		return null;
	}
	private Pattern buildUrlRegex(final String line) {
		String rule =  cleanSelectorLine(line);
		if (rule.isEmpty()) {
			return null;
		}
		int ruleIndex = 0;

		// check beginning and end
		final boolean isHttpWildCard = rule.startsWith(Terminal.HTTP_WILDCARD.value());
		if (isHttpWildCard) {
			rule = rule.substring(Terminal.HTTP_WILDCARD.length());
		}
		final boolean isStarts = rule.startsWith(Terminal.STARTS.value());
		if (isStarts) {
			rule = rule.substring(Terminal.STARTS.length());
		}
		final boolean isEnds = rule.startsWith(Terminal.ENDS.value());
		if (isEnds) {
			rule = rule.substring(Terminal.ENDS.length());
		}

		// get wildcard parts
		final String[] parts = rule.split(Regex.SPLIT_RULE.value());

		// build regex
		StringBuilder regex = new StringBuilder();
		for(final String part: parts) {
			regex.append(part.isEmpty()? "": Pattern.quote(part));
			if (rule.length() > ruleIndex + part.length()) {
				switch (rule.charAt(ruleIndex + part.length())) {
				case '^':
					regex.append( Regex.SEPARATOR.value());
					ruleIndex += part.length() + 1;
					break;
				case '*':
					regex.append( Regex.WILDCARD.value());
					ruleIndex += part.length() + 1;
					break;
				}
			}
		}

		if (regex.length()<=0 && !rule.isEmpty()) {
			regex.append( rule
					.replaceAll("\\" + Terminal.SEPARATOR.value(), Regex.SEPARATOR.value())
					.replaceAll("\\" + Terminal.WILDCARD.value(), Regex.WILDCARD.value()));
		}

		if (isHttpWildCard) {
			regex.insert(0,Regex.HTTP_WILDCARD.value());
		} else if (isStarts) {
			regex.insert(0,Regex.STARTS.value());
		} else if (regex.indexOf(Regex.STARTS.value()) !=0) {
			regex.insert(0,Regex.WILDCARD.value());
		}

		if (isEnds) {
			regex.append(Regex.ENDS.value());
		} else if (regex.lastIndexOf(Regex.WILDCARD.value()) != regex.length()-Regex.WILDCARD.value().length()) {//regex not end with  Regex.WILDCARD
			regex.append(Regex.WILDCARD.value());
		}

		return Pattern.compile(regex.toString());
	}
	boolean isComment(String line){
		return line.isEmpty()
				|| line.startsWith(Terminal.COMMENT.value())
				|| line.startsWith(Terminal.SECTION.value());
	}
	boolean isHtmlRule(String line){
		return !isComment(line) && (line.contains(Terminal.SELECTOR.value())||line.contains(Terminal.EXCEPT_SELECTOR.value()));
	}
	boolean isException(String line){
		return !isComment(line) && line.startsWith(Terminal.EXCEPTION.value());
	}
	boolean containsOpt(String line){
		return !isComment(line) &&	line.contains(Terminal.RULE_OPT.value());
	}
	boolean containsScriptOpt(String line){
		return containsOpt(line) &&	line.indexOf("script", line.indexOf(Terminal.RULE_OPT.value()))>0 && ! line.contains("~script") ;
	}
	boolean containsDomainOpt(String line){
		int fromIndex = line.indexOf(Terminal.RULE_OPT.value());
		return containsOpt(line) &&	line.indexOf("domain=", fromIndex)>=0 && line.indexOf("domain=~",fromIndex)<0 ;
	}
	String getRawOpts(String line){
		if(containsOpt(line)) {
			return line.substring(line.indexOf(Terminal.RULE_OPT.value()));
		}
		return "";
	}
}
