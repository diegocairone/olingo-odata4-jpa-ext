package com.cairone.olingo.ext.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.olingo.ext.demo.entities.PersonEntity;

public interface PersonRepository extends JpaRepository<PersonEntity, Integer>, QueryDslPredicateExecutor<PersonEntity> {

}
