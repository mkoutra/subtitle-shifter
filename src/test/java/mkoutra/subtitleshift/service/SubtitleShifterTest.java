package mkoutra.subtitleshift.service;

import mkoutra.subtitleshift.config.StorageProperties;
import mkoutra.subtitleshift.model.Attachment;
import org.apache.tika.Tika;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SubtitleShifterTest {

    @Autowired
    private SubtitleShifter subtitleShifter;

    @Autowired
    private StorageProperties storageProperties;

    @Test
    public void testSubtitleReading() throws IOException {
        Attachment attachment = new Attachment();
        attachment.setUuid(UUID.randomUUID());
        attachment.setOriginalFileName("Fallen1.srt");
        attachment.setSavedName("Fallen1.srt");
        attachment.setExtension(".srt");
        Path filepath = Paths.get(storageProperties.getUploadDir(), attachment.getSavedName());
        attachment.setFilepath(filepath);

        subtitleShifter.applyShift(attachment, "1234");
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