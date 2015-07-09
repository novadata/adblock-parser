package com.novacloud.data.adblock.loader;

import com.novacloud.data.adblock.model.Rule;
import com.novacloud.data.adblock.parser.Parser;
import com.novacloud.data.adblock.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;

public class InternetLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(InternetLoader.class);

	private final URL url;

	public InternetLoader(final URL url) {
		this.url = url;
	}

	public List<Rule> load() {
		LOGGER.info("loading {}", url);
		final long start = System.currentTimeMillis();
		List<Rule> easyList = new ArrayList<>();
					// avoid handler factory re-entrance
			@SuppressWarnings("restriction")
			final URLStreamHandler handler = "https".equals(url.getProtocol())
					? new sun.net.www.protocol.https.Handler()
					: new sun.net.www.protocol.http.Handler();
		try(InputStream in = new URL(null, url.toString(), handler).openStream();BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
			final Parser parser = new Parser();
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					final Rule rule = parser.parse(line);
					easyList.add(rule);
				} catch (final ParserException e) {
					LOGGER.error("parsing {}", line, e);
				}
			}
			LOGGER.info("loaded {} rules (in {}ms)", easyList.size(), System.currentTimeMillis() - start);
		} catch (final IOException e) {
			LOGGER.error("reading {}", url, e);
		}

		return easyList;
	}
}