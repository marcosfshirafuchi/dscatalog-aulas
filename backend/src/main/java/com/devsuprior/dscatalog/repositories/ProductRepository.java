package com.devsuprior.dscatalog.repositories;

import com.devsuprior.dscatalog.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Anotação Repository
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
