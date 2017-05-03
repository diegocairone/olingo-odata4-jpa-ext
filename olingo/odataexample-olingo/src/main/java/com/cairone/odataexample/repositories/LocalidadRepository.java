package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.entities.LocalidadPKEntity;

public interface LocalidadRepository extends JpaRepository<LocalidadEntity, LocalidadPKEntity>, QueryDslPredicateExecutor<LocalidadEntity> {

}
