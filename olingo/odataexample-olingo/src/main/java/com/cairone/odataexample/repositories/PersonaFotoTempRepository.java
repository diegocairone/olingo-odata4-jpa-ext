package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.PersonaFotoTempEntity;

public interface PersonaFotoTempRepository extends JpaRepository<PersonaFotoTempEntity, String>, QueryDslPredicateExecutor<PersonaFotoTempEntity> {

}
