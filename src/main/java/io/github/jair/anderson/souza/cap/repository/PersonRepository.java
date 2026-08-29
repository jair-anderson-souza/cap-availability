package io.github.jair.anderson.souza.cap.repository;

import io.github.jair.anderson.souza.cap.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
