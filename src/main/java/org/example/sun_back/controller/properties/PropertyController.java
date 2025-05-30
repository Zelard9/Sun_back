package org.example.sun_back.controller.properties;

import lombok.RequiredArgsConstructor;
import org.example.sun_back.entity.property.DTOs.PropertyCreateDTO;
import org.example.sun_back.entity.property.DTOs.PropertyFilterDTO;
import org.example.sun_back.entity.property.DTOs.PropertyResponseDTO;
import org.example.sun_back.entity.property.DTOs.PropertyUpdateDTO;
import org.example.sun_back.entity.property.Property;
import org.example.sun_back.entity.user.UserModel;
import org.example.sun_back.service.properties.service.PropertyService;
import org.example.sun_back.service.users.service.AuthService;
import org.example.sun_back.service.users.serviceImpl.AuthServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final AuthServiceImpl authService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponseDTO> createProperty(
            @RequestPart("data") PropertyCreateDTO dto,
            @RequestPart("images") List<MultipartFile> images
    ) {
        String email = authService.getAuthenticatedEmail();
        PropertyResponseDTO created = propertyService.createProperty(email, dto, images);
        return ResponseEntity.ok(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @RequestPart("data") PropertyUpdateDTO dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages
    ) {
        String email = authService.getAuthenticatedEmail();
        Property updated = propertyService.updateProperty(email, id, dto, newImages);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(@PathVariable Long id) {
        String email = authService.getAuthenticatedEmail();
        propertyService.deleteProperty(email, id);
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/me")
    public ResponseEntity<List<PropertyResponseDTO>> getMyProperties() {
        String email = authService.getAuthenticatedEmail();
        return ResponseEntity.ok(propertyService.getPropertiesByUser(email));
    }

    @GetMapping("all")
    public ResponseEntity<List<PropertyResponseDTO>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<PropertyResponseDTO>> filterProperties(
            PropertyFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PropertyResponseDTO> result = propertyService.filterProperties(filter, page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/favorites/{propertyId}")
    public ResponseEntity<?> addFavorite(@PathVariable Long propertyId) {
        String email = authService.getAuthenticatedEmail();
        propertyService.addPropertyToFavorites(email, propertyId);
        return ResponseEntity.ok("Added to favorites");
    }

    @DeleteMapping("/favorites/{propertyId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long propertyId) {
        String email = authService.getAuthenticatedEmail();
        propertyService.removePropertyFromFavorites(email, propertyId);
        return ResponseEntity.ok("Removed from favorites");
    }

    @GetMapping("/favorites")
    public List<PropertyResponseDTO> getFavorites() {
        String email = authService.getAuthenticatedEmail();
        return propertyService.getFavoriteProperties(email);
    }


}
