package com.kodilla.finalProject.mapper;

import com.kodilla.finalProject.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectionMapper {

    public MovieDetails movieDetailsDTOToMovieDetails(MovieDetailsDTO movieDetailsDTO) {
        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle(movieDetailsDTO.getTitle());
        movieDetails.setGenre(movieDetailsDTO.getGenre());
        movieDetails.setYear(movieDetailsDTO.getYear());
        movieDetails.setDirector(movieDetailsDTO.getDirector());
        movieDetails.setPlot(movieDetailsDTO.getPlot());

        List<Rating> ratings = movieDetailsDTO.getRatings().stream()
                .map(ratingDto -> {
                    Rating rating = Rating.builder()
                        .source(ratingDto.getSource())
                        .value(ratingDto.getValue())
                        .build();
                return rating;
                })
                .collect(Collectors.toList());

        movieDetails.setRatings(ratings);

        return movieDetails;
    }

    public MovieDetailsDTO movieDetailsToMovieDetailsDTO(MovieDetails movieDetails) {
        MovieDetailsDTO movieDetailsDTO = new MovieDetailsDTO();
        movieDetailsDTO.setTitle(movieDetails.getTitle());
        movieDetailsDTO.setGenre(movieDetails.getGenre());
        movieDetailsDTO.setYear(movieDetails.getYear());
        movieDetailsDTO.setDirector(movieDetails.getDirector());
        movieDetailsDTO.setPlot(movieDetails.getPlot());
        List<RatingDTO> ratings = movieDetails.getRatings().stream()
                .map(rating -> new RatingDTO(rating.getSource(), rating.getValue()))
                .collect(Collectors.toList());
        movieDetailsDTO.setRatings(ratings);
        return movieDetailsDTO;
    }

    public MovieProvider movieProviderDTOToMovieProvider(MovieProviderDTO movieProviderDTO) {
        return MovieProvider.builder().providerName(movieProviderDTO.getProvider_name())
                .accessType(movieProviderDTO.getAccessType())
                .build();
    }

    private MovieProviderDTO movieProviderToMovieProviderDTO (MovieProvider provider) {
        MovieProviderDTO dto = new MovieProviderDTO();
        dto.setProvider_name(provider.getProviderName());
        dto.setAccessType(provider.getAccessType());
        return dto;
    }

    public GroupedProvidersDTO movieProvidersToGroupedProvidersDTO(List<MovieProvider> providers) {
        GroupedProvidersDTO groupedProviders = new GroupedProvidersDTO();

        Map<MovieProvider.AccessType, List<MovieProviderDTO>> grouped = providers.stream()
                .map(this::movieProviderToMovieProviderDTO)
                .collect(Collectors.groupingBy(MovieProviderDTO::getAccessType));

        groupedProviders.setRental(grouped.getOrDefault(MovieProvider.AccessType.RENTAL, new ArrayList<>()));
        groupedProviders.setSubscription(grouped.getOrDefault(MovieProvider.AccessType.SUBSCRIPTION, new ArrayList<>()));
        groupedProviders.setPurchase(grouped.getOrDefault(MovieProvider.AccessType.PURCHASE, new ArrayList<>()));

        return groupedProviders;
    }

    public List<MovieProvider> movieProviderDTOListToMovieProviderList(List<MovieProviderDTO> providerDTOList) {
        return providerDTOList.stream()
                .map(this::movieProviderDTOToMovieProvider)
                .toList();
    }

    public MovieDTO mapToMovieDTO(Movie movie) {
        MovieDTO movieDTO = new MovieDTO();
        //simple fields
        movieDTO.setMovie_id(movie.getMovie_id());
        movieDTO.setTmdbId(movie.getTmdbId());
        //mappers
        movieDTO.setDetails(movieDetailsToMovieDetailsDTO(movie.getDetails()));
        movieDTO.setProviders(movieProvidersToGroupedProvidersDTO(movie.getProviders()));
        movieDTO.setPhysicalVersion(movie.getPhysicalVersion());
        return movieDTO;
    }
    public List<MovieDTO> mapToMovieList(List<Movie> movieList) {
        return movieList.stream()
                .map(this::mapToMovieDTO)
                .collect(Collectors.toList());
    }
}
