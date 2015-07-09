package com.novacloud.data.adblock;

import org.jsoup.nodes.Document;

/**
 *
 * ad blocker
 * @author <a href="mailto:zhaoxiaoyong@novacloud.com">zhaoxiaoyong</a>
 * @version Revision: 1.0
 * date 5/19/15 11:02 AM
 */
public interface AdBlocker {
    /**
     * remove ad of  html
     * @param html outer html
     * @return  html after ad removed
     */
    String removeAd(String html);

    /**
     * remove ad of src document
     * @param src  document
     * @return document after ad removed
     */
    Document removeAd(Document src);
}
