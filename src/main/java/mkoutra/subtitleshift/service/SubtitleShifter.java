package mkoutra.subtitleshift.service;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        // Input file
        Path inputDir = Paths.get(inputFilesDir);
        if (Files.notExists(inputDir)) Files.createDirectory(inputDir);
        Path inputPath = inputDir.resolve(inputFilename);

        // Output file
        Path outputDir = Paths.get(outputFilesDir);
        if (Files.notExists(outputDir)) Files.createDirectory(outputDir);
        String outputFilename = createOutputFilename(inputFilename, timeshift);
        Path outputPath = outputDir.resolve(outputFilename);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath.toFile()));
             PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    private static String createOutputFilename(String inputFilename, String timeshift) {
        String filenameExtension = inputFilename.substring(inputFilename.lastIndexOf("."));
        String filenameWithoutExtension = inputFilename.substring(0, inputFilename.lastIndexOf("."));
        String shiftSign = timeshift.startsWith("-") ? "-" : "+";
        return new StringBuilder(filenameWithoutExtension)
                .append("_")
                .append(shiftSign)
                .append(timeshift)
                .append(filenameExtension)
                .toString();
    }
}
