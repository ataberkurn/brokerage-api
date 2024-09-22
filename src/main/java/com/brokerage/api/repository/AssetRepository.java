package com.brokerage.api.repository;

import com.brokerage.api.entity.Asset;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Asset> findByCustomer_IdAndName(UUID customerId, String name);

    List<Asset> findAllByCustomer_Id(UUID customerId);
}
