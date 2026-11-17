package ma.veltra.mappers;

import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.entities.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PointArretMapper.class)
public interface TrajetMapper {

    @Mapping(target = "conducteurId", source = "conducteur.id")
    @Mapping(target = "vehiculeId", source = "vehicule.id")
    TrajetResponse toResponse(Trajet trajet);
}