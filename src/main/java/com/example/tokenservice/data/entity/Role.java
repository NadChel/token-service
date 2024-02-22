package com.example.tokenservice.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "roles")
public class Role implements GrantedAuthority {
    public static final String USER = "user";
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String authority;
    @ManyToOne
    private User user;

    public Role(String authority) {
        this.authority = authority;
    }
}
