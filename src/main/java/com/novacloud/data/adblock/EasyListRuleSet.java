package com.novacloud.data.adblock;

// https://adblockplus.org/en/filters

import com.novacloud.data.adblock.cache.LocalCache;
import com.novacloud.data.adblock.io.SerializedFile;
import com.novacloud.data.adblock.loader.InternetLoader;
import com.novacloud.data.adblock.model.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EasyListRuleSet implements RuleSet {
    private static final Logger logger = LoggerFactory.getLogger(EasyListRuleSet.class);
    private static final Config config = new Config();

    // cache to store most commons URLs
    private static final LocalCache<String, Boolean> URL_EXCEPTIONS_CACHE = new LocalCache<>("easylist_exceptions", 1500);
    private static final LocalCache<String, Boolean> URL_EXCLUSIONS_CACHE = new LocalCache<>("easylist_exclusions", 1500);

    // process time usage statistics
    private static final AtomicLong PROCESS_TIME = new AtomicLong(0);
    private static final AtomicInteger autoUpdateThreadCount = new AtomicInteger(0);
    // hit statistics
    private static final AtomicLong FILTER_HIT = new AtomicLong(0);
    private static final AtomicLong CACHE_HIT = new AtomicLong(0);
    private static final AtomicLong NB_REQUEST = new AtomicLong(0);


    private final String[] internetUrls;

    private final Set<Rule> whitelist;
    private final Set<Rule> blacklist;
//    private final static  List<Rule> scriptExclusions = new CopyOnWriteArrayList<>();
    private final static ConcurrentMap<String,ConcurrentSkipListSet<String>> selectors = new ConcurrentHashMap<>(512);
    private boolean useWhitelist;

    static {
        selectors.put("*",new ConcurrentSkipListSet<String>());
    }
    public EasyListRuleSet(final boolean useWhitelist) {
        this.internetUrls = config.getUrls();

        this.whitelist = new CopyOnWriteArraySet<>();
        this.blacklist = new CopyOnWriteArraySet<>();

        this.useWhitelist = useWhitelist;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                URL_EXCEPTIONS_CACHE.save();
                URL_EXCLUSIONS_CACHE.save();
            }
        }));

    }

    @Override
    public boolean isUseWhitelist() {
        return useWhitelist;
    }

    @Override
    public void setUseWhitelist(final boolean useWhitelist) {
        this.useWhitelist = useWhitelist;
    }

    @Override
    public int getRuleCount() {
        return blacklist.size() + whitelist.size();
    }
    @Override
    public int getExclusionRuleCount(){
        return  blacklist.size();
    }
//    public int getScriptExclusionRuleCount(){
//        int i=0;
//        for (Rule rule : blacklist) {
//            if (rule.isContainsScriptOpt()) i++;
//        }
//        return i;
//    }
//    public  List<Rule> getScriptExclusionRules(){
//        if(scriptExclusions.size()==0){
//            for (Rule rule : blacklist) {
//                if (rule.isContainsScriptOpt()) scriptExclusions.add(rule);
//            }
//        }
//        return scriptExclusions;
//    }

    @Override
    public int getExceptionRuleCount(){
        return whitelist.size();
    }
    @Override
    public boolean isEmpty() {
        return getRuleCount() == 0;
    }

    public  void add(final Rule rule) {
        switch (rule.getType()) {
            case exception:
                logger.debug("added {}", rule);
                whitelist.add(rule);
                break;
            case exclusion:
                logger.debug("added {}", rule);
                blacklist.add(rule);
                if (rule.getSelector() !=null && rule.getSelector().length()>0 && rule.isForAllDomain()){
                    selectors.get("*").add(rule.getSelector());
                }
                break;
            case empty:
                break;
        }
    }

    public void addAll(final EasyListRuleSet easyList) {
        whitelist.addAll(easyList.whitelist);
        blacklist.addAll(easyList.blacklist);
    }

//    public void replaceAll(final EasyListRuleSet easyList) {
//        whitelist.clear();
//        blacklist.clear();
//        addAll(easyList);
//    }

    @Override
    public synchronized void load() {

        final SerializedFile<Set<Rule>> localBlacklistFile = new SerializedFile<>(config.getBlacklistFilePath());
        if (!localBlacklistFile.exists() || localBlacklistFile.isOlder(Calendar.DAY_OF_YEAR, Config.EASYLIST_UPDATE_PERIOD_DAY)) {
            loadInternet();
        }else {
            try {
                //todo: old local Black and white list File version  cause load error
                final long start = System.currentTimeMillis();
                blacklist.addAll(localBlacklistFile.load());
                whitelist.addAll(new SerializedFile<Set<Rule>>(config.getWhitelistFilePath()).load());
                logger.info("loaded {} local rules (in {}ms)", EasyListRuleSet.this.getRuleCount(), System.currentTimeMillis() - start);
            } catch (ClassNotFoundException | IOException e) {
                logger.error("loading rule sets", e);
            }
        }
        loadCache();

    }

    /**
     * schedule update easylist from internet
     */
    @Override
    public void scheduleAutoUpdate() {
        if (autoUpdateThreadCount.compareAndSet(0,1)) {
            new Timer("easy list auto update", true).scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    logger.info("start auto update easy list rule set.....");

                    loadInternet();
                }
            }, Config.EASYLIST_UPDATE_PERIOD_DAY * 24 * 60 * 60 * 1000, Config.EASYLIST_UPDATE_PERIOD_DAY * 24 * 60 * 60 * 1000);
        }else {
            logger.info("have exist another auto update thread.");
        }
    }


    private void loadCache() {
        final long start = System.currentTimeMillis();
        URL_EXCEPTIONS_CACHE.load();
        URL_EXCLUSIONS_CACHE.load();
        logger.info("loaded {} URLs from cache (in {}ms)"
                , URL_EXCEPTIONS_CACHE.size() + URL_EXCLUSIONS_CACHE.size()
                , System.currentTimeMillis() - start);
    }

    protected void loadInternet() {
        blacklist.clear();
        whitelist.clear();
        for (final String url : internetUrls) {
            try {
                List<Rule> rules = new InternetLoader(new URL(url)).load();
                for (Rule rule : rules) {
                    add(rule);
                }
            } catch (final Exception e) {
                logger.error("loading {}", url, e);
            }
        }
        save();
    }

    private void save() {
        try {
            new SerializedFile<Set<Rule>>(config.getWhitelistFilePath()).save(whitelist);
        } catch (final IOException e) {
            logger.error("saving whitelist", e);
        }
        try {
            new SerializedFile<Set<Rule>>(config.getBlacklistFilePath()).save(blacklist);
        } catch (final IOException e) {
            logger.error("saving blacklist", e);
        }
    }

    @Override
    public boolean matchesWhitelist(final String url) {
        return matches(url, URL_EXCEPTIONS_CACHE, whitelist);
    }

    @Override
    public boolean matchesBlacklist(final String url) {
        return matches(url, URL_EXCLUSIONS_CACHE, blacklist);
    }

    /**
     * get all domain common ad element selectors rule list
     *
     * @return ad element selectors rule list
     */
    @Override
    public Set<String> getAdElementSelectors() {
        ConcurrentSkipListSet<String> set = selectors.get("*");
        if (set.size()<=0 ){
            for (Rule rule : blacklist) {
                if (rule.getSelector()!=null && rule.getSelector().length()>0 && rule.isForAllDomain()) {
                    set.add(rule.getSelector());
                }
            }
        }
        return set;
    }

    /**
     * get  domain specific ad element selectors rule list
     * @param domain   domain/host, such as: baidu.com
     * @return domain specific selectors. if domain is null or empty or '*', return global common selectors
     */
    @Override
    @Nonnull
    public Set<String> getAdElementSelectors(@Nullable String domain) {
        if(domain==null || domain.length()==0 ||"*".equals(domain)) {
            return getAdElementSelectors();
        }
        ConcurrentSkipListSet<String> set=selectors.get(domain);
        if (set==null){
            set = new ConcurrentSkipListSet<>();
            for (Rule rule : blacklist) {
                if (rule.getSelector()!=null && rule.getSelector().length()>0 && rule.containDomain(domain)) {
                    set.add(rule.getSelector());
                }
            }
            selectors.putIfAbsent(domain,set);
        }
        return set;
    }

//    public boolean matchesScriptExclusion(final String scriptSrc) {
//        for (final Rule rule : blacklist) {
//            if (rule.isContainsScriptOpt() && rule.applies(scriptSrc)) {
//                logger.debug("{} \"{}\" matches \"{}\" (regex={}) (original line={})"
//                        , rule.getType()
//                        , rule.getEffectiveLine()
//                        , scriptSrc
//                        , rule.getUrlRegex()
//                        , rule.getLine());
//                return true;
//            }
//        }
//        return false;
//    }

    private boolean matches(final String url, final LocalCache<String, Boolean> urlCache, final Set<Rule> rules) {
        final long timer = System.nanoTime();
        NB_REQUEST.incrementAndGet();
        Boolean match = urlCache.get(url);
        if (match != null) {
            CACHE_HIT.incrementAndGet();
        } else {
            match = matches(url, rules);
            urlCache.put(url, match);
        }
        if (match) {
            FILTER_HIT.incrementAndGet();
        }
        PROCESS_TIME.addAndGet(System.nanoTime() - timer);

        return match;
    }

    private boolean matches(final String url, final Set<Rule> rules) {
        for (final Rule rule : rules) {
            if (rule.applies(url)) {
                if(logger.isDebugEnabled()) {
                    logger.debug("{} \"{}\" matches \"{}\" (regex={}) (original line={})"
                            , rule.getType()
                            , rule.getEffectiveLine()
                            , url
                            , rule.getUrlRegex()
                            , rule.getLine());
                }
                return true;
            }
        }

        return false;
    }
}
