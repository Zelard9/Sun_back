package org.example.sun_back.entity.property.repositories;

import org.example.sun_back.entity.property.Property;
import org.example.sun_back.entity.property.applic.enums.DealType;
import org.example.sun_back.entity.property.applic.enums.PropertyType;
import org.example.sun_back.entity.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {
    List<Property> findByOwner(UserModel owner);
    List<Property> findAllByOwner(UserModel owner);
}
