package mkoutra.subtitleshift.service;

import lombok.RequiredArgsConstructor;
import mkoutra.subtitleshift.exceptions.StorageException;
import mkoutra.subtitleshift.model.Attachment;
import mkoutra.subtitleshift.model.Timestamp;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Service for applying time shifts to subtitle files.
 *
 * @author Michalis Koutrakis
 */
@Service
@RequiredArgsConstructor
public class SubtitleShifter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitleShifter.class);

    private final StorageService storageService;

    public Attachment getShiftedFile(MultipartFile file, String timeshift) throws IOException, StorageException {
        if (!timeshift.matches("^-?\\d+$")) {
            LOGGER.error("Invalid timeshift: {}", timeshift);
            throw new IllegalArgumentException("Invalid timeshift: " + timeshift);
        }

        Attachment attachment = storageService.store(file); // Save the file
        return applyShift(attachment, timeshift);
    }

    /**
     * Applies a time shift to the timestamps in a subtitle file
     * and writes the modified content to a new file.
     *
     * @param attachment                The {@link Attachment} describing the  of the input subtitle file.
     * @param timeshift                 The time shift in milliseconds (e.g., "2000" or "-1500").
     * @throws IOException              If an error occurs while reading or writing the file.
     * @throws IllegalArgumentException If the timeshift value is invalid.
     */
    public Attachment applyShift(Attachment attachment, String timeshift) throws IOException {
        Path inputPath = attachment.getFilepath();                                              // Input file path
        Path outputPath = storageService.getOutputPath(attachment.getSavedName(), timeshift);   // Output file path
        createShiftedFile(inputPath, outputPath, timeshift);

        // Shifted file
        Attachment shiftedFileAttachment = new Attachment(attachment);
        shiftedFileAttachment.setFilepath(outputPath);
        shiftedFileAttachment.setSavedName(outputPath.getFileName().toString());
        return shiftedFileAttachment;
    }

    /**
     * Reads a subtitle file, applies a time shift to timestamp lines, and writes the result to a new file.
     *
     * @param inputPath  The path to the original subtitle file.
     * @param outputPath The path where the modified file will be saved.
     * @param timeshift  The time shift (in milliseconds) to apply to the timestamps.
     * @throws IOException If an error occurs while reading or writing the file.
     */
    private void createShiftedFile(Path inputPath, Path outputPath, String timeshift) throws IOException {
        Charset charset = getCharset(inputPath.toFile());
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath.toFile(), charset));
             PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile(), charset))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (isTimeLine(line)) {
                    String shiftedTimeLine = getShiftedTimeLine(line, timeshift);
                    writer.println(shiftedTimeLine);
                    continue;
                }
                writer.println(line);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Detects the most probable character encoding of the given file.
     *
     * @param file The file whose encoding needs to be determined.
     * @return The detected Charset of the file.
     * @throws IOException If an error occurs while reading the file.
     */
    private Charset getCharset(File file) throws IOException {
        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(new BufferedInputStream(new FileInputStream(file)));
        CharsetMatch match = charsetDetector.detect();
        return Charset.forName(match.getNormalizedName());
    }

    /**
     * Adjusts the timestamps in a subtitle "time line" based on the given time shift.
     *
     * @param line      A subtitle "time line" in the format "hh:mm:ss,xxx --> hh:mm:ss,xxx".
     * @param timeshift The time shift in milliseconds.
     * @return          The modified "time line" with adjusted timestamps.
     */
    private static String getShiftedTimeLine(String line, String timeshift) {
        List<Timestamp> timestamps = Arrays.stream(line.split(" --> ")).map(Timestamp::new).toList();
        timestamps.forEach(t -> t.shift(timeshift));
        return new StringBuilder(timestamps.get(0).toString())
                .append(" --> ")
                .append(timestamps.get(1).toString())
                .toString();
    }


    /**
     * Checks if a line contains a timestamp in the format "hh:mm:ss,xxx --> hh:mm:ss,xxx".
     *
     * @param line A line from the subtitle file.
     * @return True if the line contains a timestamp, otherwise false.
     */
    private boolean isTimeLine(String line) {
        return line.matches("^\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}$");
    }
}
