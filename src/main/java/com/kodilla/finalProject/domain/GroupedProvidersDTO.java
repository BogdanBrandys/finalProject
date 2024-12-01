package com.kodilla.finalProject.domain;

import lombok.Data;

import java.util.List;

@Data
public class GroupedProvidersDTO {
    private List<MovieProviderDTO> rental;
    private List<MovieProviderDTO> subscription;
    private List<MovieProviderDTO> purchase;
}
