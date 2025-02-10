package mkoutra.subtitleshift.controller;

import lombok.RequiredArgsConstructor;
import mkoutra.subtitleshift.exceptions.StorageException;
import mkoutra.subtitleshift.model.Attachment;
import mkoutra.subtitleshift.service.StorageService;
import mkoutra.subtitleshift.service.SubtitleShifter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class FileUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    private final SubtitleShifter subtitleShifter;
    private final StorageService storageService;

    @PostMapping("/upload")
    public String handleUploadFile(@RequestParam("subtitleFile") MultipartFile file,
                                   @RequestParam("timeshift") String timeshift) throws StorageException, IOException {
        Attachment attachment = storageService.store(file);     // Save the file
        subtitleShifter.applyShift(attachment, timeshift);

        return "redirect:/";
    }

}
