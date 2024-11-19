package com.kodilla.finalProject.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RatingDTO {
    @JsonProperty("Source")
    private String source;
    @JsonProperty("Value")
    private String value;
}
