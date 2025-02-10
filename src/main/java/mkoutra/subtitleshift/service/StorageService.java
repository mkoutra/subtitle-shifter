package mkoutra.subtitleshift.service;

import lombok.RequiredArgsConstructor;
import mkoutra.subtitleshift.config.StorageProperties;
import mkoutra.subtitleshift.exceptions.StorageException;
import mkoutra.subtitleshift.model.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for managing storage paths for subtitle files.
 *
 * @author Michalis Koutrakis
 */
@Service
@RequiredArgsConstructor
public class StorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final StorageProperties storageProperties;

    public Attachment store(MultipartFile file) throws StorageException, IOException {
        if (file == null || file.isEmpty()) {
            LOGGER.error("Failed to store null or empty file.");
            throw new StorageException("Failed to store uploaded file.");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        if (!extension.equals(storageProperties.getValidExtension())) {
            LOGGER.error("Failed to save file with invalid extension: {}", extension);
            throw new StorageException("Invalid file extension: " + originalFileName +
                    ". Required extension: " + storageProperties.getValidExtension());
        }

        UUID uuid = UUID.randomUUID();
        String savedName = uuid.toString().substring(0, 10) + originalFileName;
        Path filepath = Paths.get(storageProperties.getUploadDir(), savedName);

        Files.createDirectories(filepath.getParent());  // Create the parent directories if they do not exist.
        Files.write(filepath, file.getBytes());         // Save the file to the target filepath

        return new Attachment(originalFileName, uuid, savedName, extension, filepath);
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
    public Path getOutputPath(String inputFilename, String timeshift) throws IOException {
        Path outputDir = Paths.get(storageProperties.getShiftedDir());
        if (Files.notExists(outputDir)) {
            Files.createDirectory(outputDir);
            LOGGER.debug("Output dir: {} created successfully.", outputDir);
        }
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
    public Path getInputPath(String inputFilename) throws IOException {
        Path inputDir = Paths.get(storageProperties.getUploadDir());
        if (Files.notExists(inputDir)) {
            Files.createDirectory(inputDir);
            LOGGER.debug("Input dir: {} created successfully.", inputDir);
        }
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
    public static String createOutputFilename(String inputFilename, String timeshift) {
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
