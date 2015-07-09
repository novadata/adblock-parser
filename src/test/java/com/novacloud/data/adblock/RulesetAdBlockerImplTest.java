package com.novacloud.data.adblock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class RulesetAdBlockerImplTest {


    @Test(expected = NullPointerException.class)
    public void testLazyload() throws Exception {
        RulesetAdBlockerImpl.getDomainName("hell");
        System.err.printf("testLazyload----------------\n");
        System.err.flush();
    }

    @Test
    @Ignore
    public void testTmall() throws Exception {
        final RulesetAdBlockerImpl rulesetAdBlocker = RulesetAdBlockerImpl.getInstance();
        Document src = Jsoup.connect("http://www.tmall.com").timeout(10000).get();
        long start = System.currentTimeMillis();
        rulesetAdBlocker.removeAd(src);
        long end = System.currentTimeMillis();
        System.err.printf("tmall removeAd elapsed: %s ms \n", (end-start)); //2230ms, 765ms ,833ms
    }
    @Test
    public void testYifen() throws Exception {
        final RulesetAdBlockerImpl rulesetAdBlocker = RulesetAdBlockerImpl.getInstance();
        Document src = Jsoup.connect("http://www.yifen.com").timeout(10000).get();
        long start = System.currentTimeMillis();
        rulesetAdBlocker.removeAd(src);
        long end = System.currentTimeMillis();
        System.err.printf("yifen.com removeAd elapsed: %s ms \n", (end-start)); //382ms,394ms ,225ms,381ms
    }
    @Test
    @Ignore
    public void testRemoveOurglocal() throws Exception {
        RulesetAdBlockerImpl adBlocker=RulesetAdBlockerImpl.getInstance();
        Document src = Jsoup.connect("http://www.ourglocal.com").timeout(5000).get();
        //region unescape test
        //       String unescape =CssUnescapeUtil.unescape("#Meebo\\:AdElement\\.Root");
//        System.err.println(unescape);
//        src.select(unescape);
//        unescape =CssUnescapeUtil.unescape("Ad Element Selector error:#center_col > #\\5f Emc");
//        System.err.println(unescape);
//        src.select(unescape);
        //endregion
        System.err.println("baseurl:" + src.baseUri());
//        System.err.printf("before removed ad:%s\n", src.select("#top_ad").toString());
        long start = System.currentTimeMillis();
        Document actual = adBlocker.removeAd(src);
        long end = System.currentTimeMillis();
        System.err.println("elapsed time:(ms)"+(start-end));
        System.err.printf("ourglocal remove href:%s \n", actual.select("[_href]").toString());
        Elements srcElements = actual.select("[_src]");
        System.err.printf("ourglocal remove src:%s \n", srcElements.toString());
        Assert.assertEquals(0, actual.select("[_href]").size());
        Assert.assertEquals(1, srcElements.size());
        Assert.assertTrue(srcElements.toString().contains("_src=\"/advertise/alternate.php?width=728&amp;adType=1"));
        System.err.printf("after removed ad:%s\n",actual.select("#top_ad").toString());
        System.err.printf("ourglocal remove ad:%s \n", actual.select("[_ad]").toString());
        Assert.assertEquals(3, actual.select("[_ad]").size());


    }
    @Test
    @Ignore
    public void testRemoveEmuch()throws Exception {
        RulesetAdBlockerImpl adBlocker=RulesetAdBlockerImpl.getInstance();
        Document before=Jsoup.connect("http://emuch.net/bbs/journal_cn.php?view=detail&jid=1024").timeout(5000).get();
        long start = System.currentTimeMillis();
        Document actual = adBlocker.removeAd(before);
        long end = System.currentTimeMillis();
        System.err.println("elapsed time:(ms)"+(start-end));
        Elements srcElements = actual.select("[_src]");
        System.err.println("before src element:\n" + before.select("[src]"));
        System.err.printf("emuch remove href:%s \n", actual.select("[_href]").toString());
        System.err.printf("emuch remove src:%s \n", srcElements.toString());
        Assert.assertEquals(0, actual.select("[_href]").size());
        Assert.assertEquals(1, srcElements.size());
        Assert.assertTrue(srcElements.toString().contains("http://cbjs.baidu.com/js/m.js"));
        //one ad script  is child of ad element
        System.err.printf("emuch remove ad:%s \n", actual.select("[_ad]").toString());
        Assert.assertEquals(2, actual.select("[_ad]").size());
    }

}