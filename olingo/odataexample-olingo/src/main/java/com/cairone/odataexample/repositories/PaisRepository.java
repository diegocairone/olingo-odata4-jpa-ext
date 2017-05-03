package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.PaisEntity;

public interface PaisRepository extends JpaRepository<PaisEntity, Integer>, QueryDslPredicateExecutor<PaisEntity> {

}
