package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.PersonaSectorPKEntity;

public interface PersonaSectorRepository extends JpaRepository<PersonaSectorEntity, PersonaSectorPKEntity>, QueryDslPredicateExecutor<PersonaSectorEntity> {

}
