package com.fintech.dpf_wallet_service.repository;

import com.fintech.dpf_wallet_service.domain.Payment;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByReferenceId(@NotBlank String referenceId);
    List<Payment> findBySourceWalletId(UUID walletId);
    List<Payment> findByDestinationWalletId(UUID walletId);
//    List<Payment> findByTransferId(UUID transferId);
}
