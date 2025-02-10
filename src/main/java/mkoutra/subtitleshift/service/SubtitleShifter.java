package mkoutra.subtitleshift.service;

import lombok.RequiredArgsConstructor;
import mkoutra.subtitleshift.model.Attachment;
import mkoutra.subtitleshift.model.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    /**
     * Applies a time shift to the timestamps in a subtitle file
     * and writes the modified content to a new file.
     *
     * @param attachment                The {@link Attachment} describing the  of the input subtitle file.
     * @param timeshift                 The time shift in milliseconds (e.g., "2000" or "-1500").
     * @throws IOException              If an error occurs while reading or writing the file.
     * @throws IllegalArgumentException If the timeshift value is invalid.
     */
    public void applyShift(Attachment attachment, String timeshift) throws IOException {
        if (!timeshift.matches("^-?\\d+$")) {
            LOGGER.error("Invalid timeshift: {}", timeshift);
            throw new IllegalArgumentException("Invalid timeshift: " + timeshift);
        }

        Path inputPath = attachment.getFilepath();                                              // Input file path
        Path outputPath = storageService.getOutputPath(attachment.getSavedName(), timeshift);   // Output file path
        String line;

        try (
             BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath.toFile()), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath.toFile()), StandardCharsets.UTF_8))
        ) {
//            BufferedReader reader = new BufferedReader(new FileReader(inputPath.toFile(), StandardCharsets.UTF_8));
//             PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile(), StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                if (isTimeLine(line)) {
                    String shiftedTimeLine = getShiftedTimeLine(line, timeshift);
//                    writer.println(shiftedTimeLine);
                    writer.write(shiftedTimeLine);
                    writer.newLine();
                    continue;
                }
                System.out.println(line);
//                writer.println(line);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Applies a time shift to the timestamps in a subtitle file
     * and writes the modified content to a new file.
     *
     * @param inputFilename             The name of the input subtitle file.
     * @param timeshift                 The time shift in milliseconds (e.g., "2000" or "-1500").
     * @throws IOException              If an error occurs while reading or writing the file.
     * @throws IllegalArgumentException If the timeshift value is invalid.
     */
    public void applyShift(String inputFilename, String timeshift) throws IOException {
        if (!timeshift.matches("^-?\\d+$")) {
            LOGGER.error("Invalid timeshift: {}", timeshift);
            throw new IllegalArgumentException("Invalid timeshift: " + timeshift);
        }

        Path inputPath = storageService.getInputPath(inputFilename);               // Input file path
        Path outputPath = storageService.getOutputPath(inputFilename, timeshift);  // Output file path
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath.toFile()));
             PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
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
