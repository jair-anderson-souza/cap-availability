package io.github.jair.anderson.souza.cap.mapper;

import io.github.jair.anderson.souza.cap.dto.PersonRequest;
import io.github.jair.anderson.souza.cap.model.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class PersonMapper {

    public abstract Person toPerson(PersonRequest personRequest);

}
