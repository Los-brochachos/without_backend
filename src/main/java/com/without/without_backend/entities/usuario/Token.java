package com.without.without_backend.entities.usuario;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private String token;
  @Column(name = "access_type", nullable = false)
  @Builder.Default
  private String accessType = "BEARER";
  @Enumerated(EnumType.STRING)
  private TokenType type;
  @Column(name = "date_creation", nullable = false)
  @Builder.Default
  private LocalDateTime creationDate = LocalDateTime.now();  
  @Column(name = "date_expiration", nullable = false)
  private LocalDateTime expirationDate;
  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private Usuario user;

  public enum TokenType {
    TOKEN, REFRESH_TOKEN
  }
}
