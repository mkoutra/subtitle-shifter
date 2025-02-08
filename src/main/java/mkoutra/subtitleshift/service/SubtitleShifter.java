package mkoutra.subtitleshift.service;

import lombok.Getter;
import lombok.Setter;
import mkoutra.subtitleshift.model.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Service for applying time shifts to subtitle files.
 *
 * @author Michalis Koutrakis
 */
@Getter
@Setter
@Service
public class SubtitleShifter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitleShifter.class);

    @Value("${file.storage.uploaded}")
    private String inputFilesDir;

    @Value("${file.storage.shifted}")
    private String outputFilesDir;

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

        Path inputPath = getInputPath(inputFilename);               // Input file path
        Path outputPath = getOutputPath(inputFilename, timeshift);  // Output file path
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
     * Adjusts the timestamps in a subtitle time line based on the given time shift.
     *
     * @param line      A subtitle time line in the format "hh:mm:ss,xxx --> hh:mm:ss,xxx".
     * @param timeshift The time shift in milliseconds.
     * @return          The modified time line with adjusted timestamps.
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
     * Checks if a line contains a timestamp in
     * the format "hh:mm:ss,xxx --> hh:mm:ss,xxx".
     *
     * @param line A line from the subtitle file.
     * @return True if the line contains a timestamp, otherwise false.
     */
    private boolean isTimeLine(String line) {
        return line.matches("^\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}$");
    }

    /**
     * Returns the output file path where the shifted subtitles will be saved.
     * Creates the output directory if it does not exist.
     *
     * @param inputFilename The name of the input subtitle file.
     * @param timeshift     The time shift applied to the file.
     * @return              The path to the output file.
     * @throws IOException  If an error occurs while accessing the directory.
     */
    private Path getOutputPath(String inputFilename, String timeshift) throws IOException {
        Path outputDir = Paths.get(outputFilesDir);
        if (Files.notExists(outputDir)) Files.createDirectory(outputDir);
        String outputFilename = createOutputFilename(inputFilename, timeshift);
        Path outputPath = outputDir.resolve(outputFilename);
        return outputPath;
    }

    /**
     * Returns the input file path for the given filename.
     * Creates the input directory if it does not exist.
     *
     * @param inputFilename The name of the input subtitle file.
     * @return              The path to the input file.
     * @throws IOException  If an error occurs while accessing the directory.
     */
    private Path getInputPath(String inputFilename) throws IOException {
        Path inputDir = Paths.get(inputFilesDir);
        if (Files.notExists(inputDir)) Files.createDirectory(inputDir);
        Path inputPath = inputDir.resolve(inputFilename);
        return inputPath;
    }

    /**
     * Generates a filename for the shifted subtitle file
     * based on the original filename and time shift.
     *
     * @param inputFilename The original subtitle filename.
     * @param timeshift     The time shift applied.
     * @return              The new filename with the time shift included.
     */
    private static String createOutputFilename(String inputFilename, String timeshift) {
        String filenameExtension = inputFilename.substring(inputFilename.lastIndexOf("."));
        String filenameWithoutExtension = inputFilename.substring(0, inputFilename.lastIndexOf("."));
        String shiftSign = timeshift.startsWith("-") ? "" : "+";
        return new StringBuilder(filenameWithoutExtension)
                .append("_")
                .append(shiftSign)
                .append(timeshift)
                .append(filenameExtension)
                .toString();
    }
}
