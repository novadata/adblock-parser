package com.novacloud.data.adblock;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EasyListRuleSetTest {
	final static RuleSet ruleSet = new EasyListRuleSet(false);
	@BeforeClass
	public static void init(){
		ruleSet.load();
	}
	@Test
	public void filter() {

		boolean match = ruleSet.matchesBlacklist("http://cbjs.baidu.com/a.js");
		assertTrue(match);
	}

	@Test
	public  void testSelector(){

		Set<String> elementSelectors = ruleSet.getAdElementSelectors();
		int i=0;
		for (String elementSelector : elementSelectors) {

			System.err.printf("%d: %s\n",++i,elementSelector);
		}
		assertEquals(15581,elementSelectors.size());
		assertTrue(elementSelectors.contains("#top_ad"));
	}
}