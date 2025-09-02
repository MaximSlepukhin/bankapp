package com.github.maximslepukhin.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue
    Long id;
    @Column(unique=true) String username;
    String passwordHash;
    String firstName;
    String lastName;
    String email;
    LocalDate birthDate;
    // roles, enabled, createdAt...
}