package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.PersonaFotoEntity;
import com.cairone.odataexample.entities.PersonaFotoPKEntity;

public interface PersonaFotoRepository extends JpaRepository<PersonaFotoEntity, PersonaFotoPKEntity>, QueryDslPredicateExecutor<PersonaFotoEntity> {

}
