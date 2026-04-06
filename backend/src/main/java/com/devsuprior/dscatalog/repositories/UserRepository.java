package com.devsuprior.dscatalog.repositories;

import com.devsuprior.dscatalog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Anotação Repository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
