package com.devsuprior.dscatalog.repositories;

import com.devsuprior.dscatalog.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Anotação Repository
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
