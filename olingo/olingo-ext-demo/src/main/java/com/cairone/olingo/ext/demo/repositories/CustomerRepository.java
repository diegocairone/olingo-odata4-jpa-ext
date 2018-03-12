package com.cairone.olingo.ext.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.olingo.ext.demo.entities.CustomerEntity;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer>, QueryDslPredicateExecutor<CustomerEntity> {

}
