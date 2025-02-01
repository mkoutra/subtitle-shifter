package mkoutra.subtitleshift.service;

import lombok.Getter;
import lombok.Setter;
import mkoutra.subtitleshift.entity.Timestamp;
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

@Getter
@Setter
@Service
public class SubtitleShifter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitleShifter.class);

    @Value("${file.storage.uploaded}")
    private String inputFilesDir;

    @Value("${file.storage.shifted}")
    private String outputFilesDir;

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
     * Returns a "time line" of the hh:mm:ss,xxx --> hh:mm:ss,xxx
     * after applying the specified timeshift on the timestamps.
     *
     * @param line
     * @param timeshift
     * @return
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
     * Returns true if the file is a line with timestamps of the form:
     * hh:mm:ss,xxx --> hh:mm:ss,xxx
     * @param line  A line from the file.
     * @return      True if the line contains timestamps, otherwise false.
     */
    private boolean isTimeLine(String line) {
        return line.matches("^\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}$");
    }

    private Path getOutputPath(String inputFilename, String timeshift) throws IOException {
        Path outputDir = Paths.get(outputFilesDir);
        if (Files.notExists(outputDir)) Files.createDirectory(outputDir);
        String outputFilename = createOutputFilename(inputFilename, timeshift);
        Path outputPath = outputDir.resolve(outputFilename);
        return outputPath;
    }

    private Path getInputPath(String inputFilename) throws IOException {
        Path inputDir = Paths.get(inputFilesDir);
        if (Files.notExists(inputDir)) Files.createDirectory(inputDir);
        Path inputPath = inputDir.resolve(inputFilename);
        return inputPath;
    }

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
