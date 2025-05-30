package org.example.sun_back.service.properties.service;

import org.example.sun_back.entity.property.DTOs.PropertyResponseDTO;
import org.example.sun_back.entity.property.Property;
import org.example.sun_back.entity.property.DTOs.PropertyCreateDTO;
import org.example.sun_back.entity.property.DTOs.PropertyUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PropertyService {
    PropertyResponseDTO createProperty(String userEmail, PropertyCreateDTO dto, List<MultipartFile> images);
    Property updateProperty(String userEmail, Long id, PropertyUpdateDTO dto, List<MultipartFile> newImages);
    void deleteProperty(String userEmail, Long id);
    Property getPropertyById(Long id);
    List<Property> getPropertiesByUser(String userEmail);
    List<Property> getAllProperties();
}
