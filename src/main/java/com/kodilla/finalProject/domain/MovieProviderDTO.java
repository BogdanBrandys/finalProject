package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieProviderDTO {
    private String provider_name;
    private MovieProvider.AccessType accessType;
}
