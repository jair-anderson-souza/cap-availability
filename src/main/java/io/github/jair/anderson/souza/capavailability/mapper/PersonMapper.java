package io.github.jair.anderson.souza.capavailability.mapper;

import io.github.jair.anderson.souza.capavailability.dto.PersonRequest;
import io.github.jair.anderson.souza.capavailability.model.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class PersonMapper {

    public abstract Person toPerson(PersonRequest personRequest);

}
