package ma.veltra.mappers;

import ma.veltra.dto.response.VehiculeResponse;
import ma.veltra.entities.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "conducteurId", source = "conducteur.id")
    VehiculeResponse toResponse(Vehicule vehicule);
}