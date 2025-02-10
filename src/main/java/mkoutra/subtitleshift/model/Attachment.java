package mkoutra.subtitleshift.model;

import lombok.*;

import java.nio.file.Path;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Attachment {
    private String originalFileName;
    private UUID uuid;
    private String savedName;
    private String extension;
    private Path filepath;
}
