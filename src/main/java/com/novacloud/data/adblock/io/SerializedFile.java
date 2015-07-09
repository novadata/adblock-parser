package com.novacloud.data.adblock.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Calendar;

@SuppressWarnings("serial")
public class SerializedFile<K> extends File {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializedFile.class);

    public SerializedFile(final String filepath) {
        super(filepath);
    }

    @SuppressWarnings("unchecked")
    public K load() throws ClassNotFoundException, IOException {
        LOGGER.info("loading {}", this);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this));
             ObjectInputStream ois = new ObjectInputStream(bis);
        ) {
            return (K) ois.readObject();
        }
    }

    public void save(final K k) throws IOException {
        LOGGER.info("saving {}", this);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this));ObjectOutputStream oos = new ObjectOutputStream(bos);) {

            oos.writeObject(k);
        }
    }

    public boolean isOlder(final int field, final int value) {
        try {
            final Calendar refDate = Calendar.getInstance();
            refDate.add(field, -1 * value);
            return lastModified() < refDate.getTimeInMillis();
        } catch (final Exception e) {
            LOGGER.error("", e);
            return true;
        }
    }
}