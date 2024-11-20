package com.kodilla.finalProject.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "physical_versions")
public class PhysicalVersion {
    @Id
    @GeneratedValue
    private Long id;

    private String description;
    private int releaseYear;
    private Boolean steelbook;
    private String details;

}
