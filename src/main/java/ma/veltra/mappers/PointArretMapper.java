package ma.veltra.mappers;

import ma.veltra.dto.response.PointArretResponse;
import ma.veltra.entities.PointArret;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PointArretMapper {
    PointArretResponse toResponse(PointArret pointArret);
}