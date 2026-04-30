package com.devsuperior.dscatalog.repositories;

import com.devsuperior.dscatalog.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Anotação Repository
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
