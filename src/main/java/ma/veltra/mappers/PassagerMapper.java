package ma.veltra.mappers;

import ma.veltra.dto.request.PassagerCreateRequest;
import ma.veltra.dto.response.PassagerResponse;
import ma.veltra.entities.Passager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassagerMapper {

    @Mapping(target = "id", ignore = true)
    Passager toEntity(PassagerCreateRequest request);

    PassagerResponse toResponse(Passager passager);
}