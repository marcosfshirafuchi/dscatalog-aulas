package com.devsuprior.dscatalog.repositories;

import com.devsuprior.dscatalog.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Anotação Repository
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
