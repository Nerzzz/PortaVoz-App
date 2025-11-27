package com.example.portavoz;

import java.time.Duration;
import java.time.Instant;

public class FormatTime {
    public String formatTimeAgo(String isoDate) {
        try {
            Instant instant = Instant.parse(isoDate);
            long seconds = Duration.between(instant, Instant.now()).getSeconds();

            if (seconds < 60)
                return seconds + "s atrás";

            long minutes = seconds / 60;
            if (minutes < 60)
                return minutes + "m atrás";

            long hours = minutes / 60;
            if (hours < 24)
                return hours + "h atrás";

            long days = hours / 24;
            return days + "d atrás";

        } catch (Exception e) {
            return "";
        }
    }
}
