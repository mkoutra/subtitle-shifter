package mkoutra.subtitleshift.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the subtitle timestamps.
 *
 * @author Michalis Koutrakis
 */
@Getter
@Setter
@AllArgsConstructor
public class Timestamp {
    private static Logger LOGGER = LoggerFactory.getLogger(Timestamp.class);

    private Integer hh;
    private Integer mm;
    private Integer ss;
    private Integer xxx;

    public Timestamp(String timestamp) {
        if (!timestamp.matches("^\\d{2}:\\d{2}:\\d{2},\\d{3}$")) {
            LOGGER.error("Invalid timestamp encountered: {}", timestamp);
            throw new IllegalArgumentException("Invalid timestamp: " + timestamp);
        }

        String[] tokens = timestamp.split(":");
        int commaSeparatorIndex = tokens[2].lastIndexOf(",");

        hh = Integer.parseInt(tokens[0]);
        mm = Integer.parseInt(tokens[1]);
        ss = Integer.parseInt(tokens[2].substring(0,commaSeparatorIndex));
        xxx = Integer.parseInt(tokens[2].substring(commaSeparatorIndex + 1));
    }

    public Timestamp(int milliseconds) {
        createFromMilliseconds(milliseconds);
    }

    public Integer toMilliseconds() {
        return xxx + (ss + mm * 60 + hh * 60 * 60) * 1000;
    }

    public void shift(String timeshift) {
        Integer timeshiftInt = Integer.parseInt(timeshift);
        Integer totalMilliseconds = toMilliseconds();
        createFromMilliseconds(totalMilliseconds + timeshiftInt);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d:%02d,%03d", hh, mm, ss, xxx);
    }

    private void createFromMilliseconds(int milliseconds) {
        hh = milliseconds / (3600 * 1000);
        milliseconds %= (3600 * 1000);

        mm = milliseconds / (60 * 1000);
        milliseconds %= (60 * 1000);

        ss = milliseconds / 1000;
        xxx = milliseconds % 1000;
    }
}
