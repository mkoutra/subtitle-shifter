package mkoutra.subtitleshift.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageProperties {
    private String uploadDir;
    private String shiftedDir;
    private String validExtension;
}
