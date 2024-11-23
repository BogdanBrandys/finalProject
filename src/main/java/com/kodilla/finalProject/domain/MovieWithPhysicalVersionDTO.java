package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieWithPhysicalVersionDTO {
    private Long movie_id;
    private Long tmdbId;
    private MovieDetailsDTO details;
    private GroupedProvidersDTO providers;
    private PhysicalVersionDTO physicalVersion;
}
