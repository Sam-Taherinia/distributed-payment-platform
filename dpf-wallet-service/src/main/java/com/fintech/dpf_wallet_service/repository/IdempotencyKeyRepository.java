package com.fintech.dpf_wallet_service.repository;

import com.fintech.dpf_wallet_service.domain.IdempotencyKey;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT i FROM IdempotencyKey i WHERE i.key = :key")
//    Optional<IdempotencyKey> findByIdForUpdate(String key);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT k FROM IdempotencyKey k WHERE k.key = :key")
    Optional<IdempotencyKey> findByIdForUpdate(@Param("key") String key);

}
