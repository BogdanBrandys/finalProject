package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalVersionDTO {
    private String description;
    private int releaseYear;
    private Boolean steelbook;
    private String details;
    private String movieTitle;
}