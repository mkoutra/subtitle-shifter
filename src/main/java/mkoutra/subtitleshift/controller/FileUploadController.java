package mkoutra.subtitleshift.controller;

import lombok.RequiredArgsConstructor;
import mkoutra.subtitleshift.exceptions.StorageException;
import mkoutra.subtitleshift.model.Attachment;
import mkoutra.subtitleshift.service.SubtitleShifter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@Controller
@RequiredArgsConstructor
public class FileUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    private final SubtitleShifter subtitleShifter;

    @PostMapping(value = "/uploadAndShift", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> uploadAndShiftSubtitle(@RequestParam("subtitleFile") MultipartFile originalFile,
                                                     @RequestParam("timeshift.msg") String timeshift)
            throws StorageException, IOException {

        // Validate timeshift
        if (!timeshift.matches("^-?\\d+$")) {
            LOGGER.error("Invalid timeshift received: {}", timeshift);
            return ResponseEntity.badRequest().body(null);
        }

        Attachment shiftedFile = subtitleShifter.getShiftedFile(originalFile, timeshift);

        try {
            Path filePath = shiftedFile.getFilepath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + originalFile.getOriginalFilename())
                    .body(resource);
        } catch (MalformedURLException ex) {
            LOGGER.error("Invalid file URL: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
