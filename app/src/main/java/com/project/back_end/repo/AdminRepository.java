package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.back_end.models.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByUsername(String username);
}