package mkoutra.subtitleshift.service;

import mkoutra.subtitleshift.config.StorageProperties;
import org.apache.tika.Tika;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SubtitleShifterTest {

    @Autowired
    private SubtitleShifter subtitleShifter;

    @Autowired
    private StorageProperties storageProperties;

    @Test
    public void testSubtitleReading() throws IOException {
        subtitleShifter.applyShift("Fallen1.srt", "123");
    }

    @Test
    public void testTika() throws IOException {
        String filename = "substance2.srt";
        File file = new File(storageProperties.getUploadDir() + "/" + filename);

        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(new BufferedInputStream(new FileInputStream(file)));
        CharsetMatch match = charsetDetector.detect();
        CharsetMatch[] matches = charsetDetector.detectAll();

        System.out.println(Arrays.toString(matches));
        System.out.println(Charset.forName(match.getName()));
        System.out.println(match);

        System.out.println("Get Charset: " + getCharset(file));
    }

    private Charset getCharset(File file) throws IOException {
        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(new BufferedInputStream(new FileInputStream(file)));
        CharsetMatch match = charsetDetector.detect();
        return Charset.forName(match.getNormalizedName());
    }
}