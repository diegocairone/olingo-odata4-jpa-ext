package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.SectorEntity;

public interface SectorRepository extends JpaRepository<SectorEntity, Integer>, QueryDslPredicateExecutor<SectorEntity> {

}
