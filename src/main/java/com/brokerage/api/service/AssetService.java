package com.brokerage.api.service;

import com.brokerage.api.entity.Asset;
import com.brokerage.api.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    @CachePut(value = "assets", key = "#asset.customer.id", cacheManager = "redisCacheManager")
    public Asset save(Asset asset) {
        return assetRepository.save(asset);
    }

    @Cacheable(value = "assets", key = "#customerId + #name", cacheManager = "redisCacheManager")
    public Asset getAssetByCustomerIdAndName(UUID customerId, String name) {
        return assetRepository.findByCustomer_IdAndName(customerId, name).orElse(null);
    }

    @Cacheable(value = "assets", key = "#customerId", cacheManager = "redisCacheManager")
    public List<Asset> getAssetsByCustomerId(UUID customerId) {
        return assetRepository.findAllByCustomer_Id(customerId);
    }
}
