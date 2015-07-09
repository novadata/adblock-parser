package com.novacloud.data.adblock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * ad blocker singleton based on rule set ,lazy load ruleset <br>
 * ref:http://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java
 * @author <a href="mailto:zhaoxiaoyong@novacloud.com">zhaoxiaoyong</a>
 * @version Revision: 1.0
 *          date 13/5/2015 11:06 PM
 */
public class RulesetAdBlockerImpl implements AdBlocker {
    private static final Logger logger = LoggerFactory.getLogger(RulesetAdBlockerImpl.class);
    private final RuleSet ruleSet = new EasyListRuleSet(false);
    protected RulesetAdBlockerImpl() {
        ruleSet.load();
        ruleSet.scheduleAutoUpdate();
    }
    public static RulesetAdBlockerImpl getInstance() {
        return InstanceHolder.instance;
    }

    public static String getDomainName(String url) {

        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            logger.error("url syntax exception:{}", url);
            return "*";
        }
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    /**
     * remove ad
     *
     * @param html html
     * @return html after ad removed
     */
    @Override
    public String removeAd(String html) {
        return removeAd(Jsoup.parse(html)).html();
    }

    /**
     * remove ad from src document
     *
     * @param src src document
     * @return document that ad removed
     */
    @Override
    public Document removeAd(Document src) {
        long start = System.currentTimeMillis();
        long end = 0l;
        Elements urls = src.select("[href]");
        for (Element urlElement : urls) {
            if (ruleSet.matchesBlacklist(urlElement.attr("href"))) {
                urlElement.attr("_href", urlElement.attr("href"));
                urlElement.removeAttr("href");
            }
        }

        if (logger.isDebugEnabled()) {
            end = System.currentTimeMillis();
            logger.debug("remove href elapsed: {} ms \n", end - start);
        }
        Elements srcs = src.select("[src]");
        for (Element urlElement : srcs) {
            if (ruleSet.matchesBlacklist(urlElement.attr("src"))) {
                urlElement.attr("_src", urlElement.attr("src"));
                urlElement.removeAttr("src");
            }
        }

        if (logger.isDebugEnabled()) {
            start = end;
            end = System.currentTimeMillis();
            logger.debug("remove src elapsed: {} ms\n", end - start);
        }


        String domainName = getDomainName(src.baseUri());
        //remove ad using domain specific selectors
        Set<String> selectors = ruleSet.getAdElementSelectors(domainName);
        for (String selector : selectors) {
            try {
                src.select(selector).empty().attr("_ad", selector);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ad Element Selector error:{}", selector);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            start = end;
            end = System.currentTimeMillis();
            logger.debug("remove ad element using domain specific selector elapsed: {} ms\n", end - start);
        }
        if (selectors.size() == 0) {
            //remove ad using global selectors
            selectors = ruleSet.getAdElementSelectors();
            for (String selector : selectors) {//todo: optimize performance ,remove ad element using global selector elapsed: 2179 ms
                try {
                    src.select(selector).empty().attr("_ad", selector);
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ad Element Selector error:{}", selector);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                start = end;
                end = System.currentTimeMillis();
                logger.debug("remove ad element using global selector elapsed: {} ms\n", end - start);
            }
        }
        return src;
    }

    private static class InstanceHolder {
        public static RulesetAdBlockerImpl instance = new RulesetAdBlockerImpl();
    }

}
