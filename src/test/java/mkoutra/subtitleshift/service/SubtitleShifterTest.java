package mkoutra.subtitleshift.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SubtitleShifterTest {

    @Autowired
    private SubtitleShifter subtitleShifter;

    @Test
    public void testSubtitleReading() throws IOException {
        subtitleShifter.applyShift("Fallen1.srt", "-399");
    }
}