package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MovieBasicDTO {
        private Long id;
        private String title;
        private String release_date;

        public String getRelease_date() {
                if (release_date != null && release_date.length() >= 4) {
                        return release_date.substring(0, 4);
                }
                return null;
        }
}
