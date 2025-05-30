package org.example.sun_back.entity.property.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sun_back.entity.property.applic.enums.DealType;
import org.example.sun_back.entity.property.applic.enums.PropertyType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyUpdateDTO {
    private String title;
    private String address;
    private double area;
    private int floors;
    private int rooms;
    private boolean isNewBuilding;
    private DealType status;
    private PropertyType type;
    private String condition;
    private double price;
    private double distanceFromCenter;
    private String district;
    private String city;
    private List<String> imageUrls;
}

