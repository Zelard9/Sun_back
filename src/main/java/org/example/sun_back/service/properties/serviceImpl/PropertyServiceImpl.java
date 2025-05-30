package org.example.sun_back.service.properties.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.sun_back.entity.property.Property;
import org.example.sun_back.entity.property.applic.images.images.PropertyImage;
import org.example.sun_back.entity.property.DTOs.PropertyCreateDTO;
import org.example.sun_back.entity.property.repositories.PropertyImageRepository;
import org.example.sun_back.entity.property.repositories.PropertyRepository;
import org.example.sun_back.entity.property.DTOs.PropertyUpdateDTO;
import org.example.sun_back.entity.user.UserModel;
import org.example.sun_back.entity.user.repositories.UserRepository;
import org.example.sun_back.service.AWS3.S3Service;
import org.example.sun_back.service.properties.service.PropertyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public Property createProperty(String userEmail, PropertyCreateDTO dto, List<MultipartFile> images) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Property property = mapCreateDtoToProperty(dto);
        property.setOwner(user);

        List<PropertyImage> imageEntities = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = s3Service.uploadFile(image);
            PropertyImage propertyImage = new PropertyImage();
            propertyImage.setUrl(imageUrl);
            propertyImage.setProperty(property);
            imageEntities.add(propertyImage);
        }
        property.setImages(imageEntities);

        return propertyRepository.save(property);
    }

    @Override
    @Transactional
    public Property updateProperty(String userEmail, Long id, PropertyUpdateDTO dto, List<MultipartFile> newImages) {
        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!existing.getOwner().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not the owner of this property");
        }

        mapUpdateDtoToProperty(dto, existing);

        if (newImages != null && !newImages.isEmpty()) {
            for (PropertyImage image : existing.getImages()) {
                s3Service.deleteFileFromUrl(image.getUrl());
            }
            List<PropertyImage> updatedImages = new ArrayList<>();
            for (MultipartFile file : newImages) {
                String url = s3Service.uploadFile(file);
                PropertyImage img = new PropertyImage();
                img.setUrl(url);
                img.setProperty(existing);
                updatedImages.add(img);
            }
            existing.setImages(updatedImages);
        }

        return propertyRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteProperty(String userEmail, Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getOwner().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not the owner of this property");
        }

        for (PropertyImage image : property.getImages()) {
            s3Service.deleteFileFromUrl(image.getUrl());
        }

        propertyRepository.delete(property);
    }

    @Override
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
    }

    @Override
    public List<Property> getPropertiesByUser(String userEmail) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return propertyRepository.findAllByOwner(user);
    }

    @Override
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    private Property mapCreateDtoToProperty(PropertyCreateDTO dto) {
        Property property = new Property();
        property.setTitle(dto.getTitle());
        property.setAddress(dto.getAddress());
        property.setArea(dto.getArea());
        property.setFloors(dto.getFloors());
        property.setRooms(dto.getRooms());
        property.setNewBuilding(dto.isNewBuilding());
        property.setStatus(dto.getStatus());
        property.setType(dto.getType());
        property.setCondition(dto.getCondition());
        property.setPrice(dto.getPrice());
        property.setDistanceFromCenter(dto.getDistanceFromCenter());
        property.setDistrict(dto.getDistrict());
        property.setCity(dto.getCity());
        return property;
    }

    private void mapUpdateDtoToProperty(PropertyUpdateDTO dto, Property property) {
        property.setTitle(dto.getTitle());
        property.setAddress(dto.getAddress());
        property.setArea(dto.getArea());
        property.setFloors(dto.getFloors());
        property.setRooms(dto.getRooms());
        property.setNewBuilding(dto.isNewBuilding());
        property.setStatus(dto.getStatus());
        property.setType(dto.getType());
        property.setCondition(dto.getCondition());
        property.setPrice(dto.getPrice());
        property.setDistanceFromCenter(dto.getDistanceFromCenter());
        property.setDistrict(dto.getDistrict());
        property.setCity(dto.getCity());
    }
}

