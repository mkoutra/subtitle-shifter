package mkoutra.subtitleshift.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TimestampTest {

    @Test
    void testStringConstructor() {
        Timestamp timestamp = new Timestamp("01:01:05,123");
        assertEquals(1, timestamp.getHh());
        assertEquals(5, timestamp.getSs());
        assertEquals(123, timestamp.getXxx());
        assertEquals("01:01:05,123", timestamp.toString());
    }

    @Test
    void testMillisecondConstructor() {
        Timestamp timestamp = new Timestamp(60 * 60 * 1000 + 65123);
        assertEquals("01:01:05,123", timestamp.toString());
    }

    @Test
    void testToMilliseconds() {
        Timestamp timestamp = new Timestamp("01:01:05,123");
        assertEquals(60 * 60 * 1000 + 65123, timestamp.toMilliseconds());

        Timestamp timestamp2 = new Timestamp("00:00:00,000");
        assertEquals(0, timestamp2.toMilliseconds());
    }

    @Test
    void testShift() {
        Timestamp timestamp = new Timestamp("01:01:05,123");
        timestamp.shift("500");
        assertEquals("01:01:05,623", timestamp.toString());

        timestamp.shift("2000");
        assertEquals("01:01:07,623", timestamp.toString());
    }
}