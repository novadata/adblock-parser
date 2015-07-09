package com.novacloud.data.adblock;


import java.util.Set;

public interface RuleSet {
	int getRuleCount();

	int getExclusionRuleCount();

	int getExceptionRuleCount();

	boolean isEmpty();

	/**
	 * 加载规则.<br>
	 * 首先从本地加载，如果本地没有规则，或者规则已经过期，则从网络加载.
	 */
	void load();

	/**
	 *
	 */
	void scheduleAutoUpdate();
	/**
	 * 是否使用白名单, 白名单 not supported
	 * @param useWhitelist   if use white list
	 */
	void setUseWhitelist(boolean useWhitelist);

	/**
	 * 是否使用白名单
	 * @return  是否使用白名单
	 */
	boolean isUseWhitelist();

	/**
	 * url是否存在于白名单中
	 * @param url url
	 * @return  true if url是否存在于白名单中
	 */
	boolean matchesWhitelist(String url);

	/**
	 * url是否存在于黑名单中
	 * @param url
	 * @return
	 */
	boolean matchesBlacklist(String url);

	/**
	 * get all domain common ad element selectors rule list
	 * @return ad element selectors rule list
	 */
	Set<String> getAdElementSelectors();

	/**
	 * get  domain specific ad element selectors rule list
	 * @param domain
	 * @return
	 */
	Set<String> getAdElementSelectors(String domain);
}
