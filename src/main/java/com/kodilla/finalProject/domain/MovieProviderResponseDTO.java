package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieProviderResponseDTO {
    private Map<String, RegionProviders> results;

    @Data
    public static class RegionProviders {
        private List<ProviderData> buy;
        private List<ProviderData> rent;
        private List<ProviderData> flatrate;
    }
    @Data
    public static class ProviderData {
        private String provider_name;
    }
}
