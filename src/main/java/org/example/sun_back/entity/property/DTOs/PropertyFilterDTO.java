package org.example.sun_back.entity.property.DTOs;

import lombok.Data;
import org.example.sun_back.entity.property.applic.enums.DealType;
import org.example.sun_back.entity.property.applic.enums.PropertyType;

@Data
public class PropertyFilterDTO {
    private String city;
    private String district;
    private PropertyType type;
    private DealType status;
    private Integer minRooms;
    private Integer maxRooms;
    private Double minPrice;
    private Double maxPrice;
}