package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.PermisoEntity;

public interface PermisoRepository extends JpaRepository<PermisoEntity, String>, QueryDslPredicateExecutor<PermisoEntity> {

}
