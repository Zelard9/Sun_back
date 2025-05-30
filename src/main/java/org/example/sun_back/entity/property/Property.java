package org.example.sun_back.entity.property;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sun_back.entity.property.applic.enums.DealType;
import org.example.sun_back.entity.property.applic.enums.PropertyType;
import org.example.sun_back.entity.property.applic.images.images.PropertyImage;
import org.example.sun_back.entity.user.UserModel;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "properties")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String address;
    private double area;
    private int floors;
    private int rooms;
    private boolean isNewBuilding;

    @Enumerated(EnumType.STRING)
    private DealType status; // FOR_RENT, FOR_SALE

    @Enumerated(EnumType.STRING)
    private PropertyType type; // APARTMENT, HOUSE, GARAGE, COMMERCIAL, etc.

    private String condition; // e.g., "потребує ремонту", "гарний", "новий"
    private double price;
    private double distanceFromCenter;
    private String district;
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserModel owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PropertyImage> images;
}

