package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TMDBResponseDTO {
    private List<MovieBasicDTO> results;
}
