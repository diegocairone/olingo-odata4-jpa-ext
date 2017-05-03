package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.ProvinciaEntity;
import com.cairone.odataexample.entities.ProvinciaPKEntity;

public interface ProvinciaRepository extends JpaRepository<ProvinciaEntity, ProvinciaPKEntity>, QueryDslPredicateExecutor<ProvinciaEntity> {

}
