package com.github.uuid;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by liuchunlong on 16-12-20.
 */
public class UUIDTest {

    private static final Logger logger = LoggerFactory.getLogger("UUIDTest.class");

    @Test
    public void testNew() {

        UUID uuid = new UUID(0l,0l);
        UUID uuid2 = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        logger.info(uuid.toString());//00000000-0000-0000-0000-000000000000
        logger.info(uuid2.toString());//38400000-8cf0-11bd-b23e-10b96e4ef00d
    }

}
