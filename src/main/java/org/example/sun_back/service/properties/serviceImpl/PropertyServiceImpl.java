package org.example.sun_back.service.properties.serviceImpl;

import ch.qos.logback.core.model.PropertyModel;
import lombok.RequiredArgsConstructor;
import org.example.sun_back.entity.property.DTOs.PropertyFilterDTO;
import org.example.sun_back.entity.property.DTOs.PropertyResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public PropertyResponseDTO createProperty(String userEmail, PropertyCreateDTO dto, List<MultipartFile> images) {
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

        Property saved = propertyRepository.save(property);
        return mapToResponseDto(saved);
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
            // Видаляємо старі файли з S3
            for (PropertyImage image : existing.getImages()) {
                s3Service.deleteFileFromUrl(image.getUrl());
            }

            // Завантажуємо нові
            List<PropertyImage> updatedImages = new ArrayList<>();
            for (MultipartFile file : newImages) {
                String url = s3Service.uploadFile(file);
                PropertyImage img = new PropertyImage();
                img.setUrl(url);
                img.setProperty(existing); // встановлюємо зв'язок
                updatedImages.add(img);
            }

            // 🟢 ВАЖЛИВО: замість `setImages()` — чистимо і додаємо, щоби Hibernate знав про orphan removal
            existing.getImages().clear();
            existing.getImages().addAll(updatedImages);
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
    public PropertyResponseDTO getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        return mapToResponseDto(property);
    }

    @Override
    public List<PropertyResponseDTO> getPropertiesByUser(String userEmail) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        List<Property> properties = propertyRepository.findAllByOwner(user);
        return properties.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public List<PropertyResponseDTO> getAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        return properties.stream()
                .map(property -> mapToResponseDto(property))
                .collect(Collectors.toList());
    }

    @Override
    public Page<PropertyResponseDTO> filterProperties(PropertyFilterDTO filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Property> spec = null;

        if (filter.getCity() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.equal(root.get("city"), filter.getCity()));
        }
        if (filter.getDistrict() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.equal(root.get("district"), filter.getDistrict()));
        }
        if (filter.getType() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getStatus() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getMinRooms() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rooms"), filter.getMinRooms()));
        }
        if (filter.getMaxRooms() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.lessThanOrEqualTo(root.get("rooms"), filter.getMaxRooms()));
        }
        if (filter.getMinPrice() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
        }
        if (filter.getMaxPrice() != null) {
            spec = addSpec(spec, (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
        }

        Page<Property> pageResult = propertyRepository.findAll(spec, pageable);
        return pageResult.map(this::mapToResponseDto);
    }

    private Specification<Property> addSpec(Specification<Property> base, Specification<Property> addition) {
        return base == null ? Specification.where(addition) : base.and(addition);
    }




    private Property mapCreateDtoToProperty(PropertyCreateDTO dto) {
        Property property = new Property();
        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
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
        property.setDescription(dto.getDescription());
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

    private PropertyResponseDTO mapToResponseDto(Property property) {
        return PropertyResponseDTO.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .address(property.getAddress())
                .area(property.getArea())
                .floors(property.getFloors())
                .rooms(property.getRooms())
                .isNewBuilding(property.isNewBuilding())
                .status(property.getStatus())
                .type(property.getType())
                .condition(property.getCondition())
                .price(property.getPrice())
                .distanceFromCenter(property.getDistanceFromCenter())
                .district(property.getDistrict())
                .city(property.getCity())
                .ownerEmail(property.getOwner().getEmail())
                .phoneNumber(property.getOwner().getPhoneNumber())
                .imageUrls(property.getImages().stream()
                        .map(PropertyImage::getUrl)
                        .collect(Collectors.toList()))
                .build();
    }
}
