package com.fintech.dpf_wallet_service.repository;

import com.fintech.dpf_wallet_service.domain.Transaction;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByReferenceId(@NotBlank String referenceId);
}
