package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
