package vttp.batchb.csf.project.models;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

public class MeetingRequest {
        private String location;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dateTime;

        public String getLocation() {
            return location;
        }
        public void setLocation(String location) {
            this.location = location;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }
        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
}
