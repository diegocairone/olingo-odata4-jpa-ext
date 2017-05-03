package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.entities.UsuarioPKEntity;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UsuarioPKEntity>, QueryDslPredicateExecutor<UsuarioEntity> {

}
