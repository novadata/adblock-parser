package com.novacloud.data.adblock;


public class Config {
	public static final int EASYLIST_UPDATE_PERIOD_DAY = 7;
	private static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "/";
	private static final String EASYLIST_EXCEPTIONS_FILEPATH = TEMP_FOLDER + "easylist_exceptions.bin";
	private static final String EASYLIST_EXCLUSIONS_FILEPATH = TEMP_FOLDER + "easylist_exclusions.bin";
	private static final String[] EASY_LIST_URLS = {
//		"https://easylist-downloads.adblockplus.org/easylist.txt",
		"https://easylist-downloads.adblockplus.org/easylistchina.txt"
//		, "https://easylist-downloads.adblockplus.org/liste_fr.txt"
//		, "https://easylist-downloads.adblockplus.org/easyprivacy.txt"
//		, "https://easylist-downloads.adblockplus.org/malwaredomains_full.txt"
	};

	public String getWhitelistFilePath() {
		return EASYLIST_EXCEPTIONS_FILEPATH;
	}

	public String getBlacklistFilePath() {
		return EASYLIST_EXCLUSIONS_FILEPATH;
	}

	public String[] getUrls() {
		return EASY_LIST_URLS.clone();
	}
}
