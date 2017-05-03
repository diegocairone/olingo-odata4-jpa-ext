package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.UsuarioPermisoEntity;
import com.cairone.odataexample.entities.UsuarioPermisoPKEntity;

public interface UsuarioPermisoRepository extends JpaRepository<UsuarioPermisoEntity, UsuarioPermisoPKEntity>, QueryDslPredicateExecutor<UsuarioPermisoEntity> {

}
