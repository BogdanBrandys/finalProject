package com.kodilla.finalProject.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "providers")
public class MovieProvider {
    @Id
    @GeneratedValue
    private Long provider_id;

    private String providerName;

    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @ManyToOne
    @JoinColumn(name = "movies_id", referencedColumnName = "id", nullable = false)
    private Movie movie;

    public enum AccessType {
        SUBSCRIPTION,
        RENTAL,
        PURCHASE
    }
}
