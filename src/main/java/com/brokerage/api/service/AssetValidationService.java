package com.brokerage.api.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetValidationService {

    public boolean isAssetValid(String assetName) {
        List<String> validAssets = List.of("AAPL", "MSFT", "EUR", "TRY");

        return validAssets.contains(assetName);
    }
}
