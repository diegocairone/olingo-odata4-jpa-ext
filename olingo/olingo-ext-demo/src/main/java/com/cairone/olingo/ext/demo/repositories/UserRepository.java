package com.cairone.olingo.ext.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.cairone.olingo.ext.demo.entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer>, QuerydslPredicateExecutor<UserEntity> {

}
