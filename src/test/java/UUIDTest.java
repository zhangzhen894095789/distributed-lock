import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by 刘春龙 on 2017/5/27.
 */
public class UUIDTest {

    private static final Logger logger = LoggerFactory.getLogger(UUIDTest.class);

    @Test
    public void test() {
//        18:12:06.645 [main] INFO  UUIDTest - 00000000-0000-0000-0000-000000000000
//        18:12:06.655 [main] INFO  UUIDTest - 38400000-8cf0-11bd-b23e-10b96e4ef00d
        UUID uuid = new UUID(0l,0l);
        logger.info(uuid.toString());
        uuid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        logger.info(uuid.toString());
    }

    @Test
    public void test2() {
//        18:13:01.076 [main] INFO  UUIDTest - uuid == uuid2 ? true
        UUID uuid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        UUID uuid2 = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        logger.info("uuid == uuid2 ? " + uuid.equals(uuid2));
    }
}
