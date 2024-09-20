package com.brokerage.api.service;

import com.brokerage.api.entity.Asset;
import com.brokerage.api.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public void save(Asset asset) {
        assetRepository.save(asset);
    }

    public Asset getAssetByCustomerIdAndName(UUID customerId, String name) {
        return assetRepository.findByCustomer_IdAndName(customerId, name).orElse(null);
    }

    public List<Asset> getAssetsByCustomerId(UUID customerId) {
        return assetRepository.findAllByCustomer_Id(customerId);
    }
}
