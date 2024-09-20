package com.brokerage.api.controller;

import com.brokerage.api.annotation.CheckCustomerAccess;
import com.brokerage.api.entity.Asset;
import com.brokerage.api.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @CheckCustomerAccess
    public List<Asset> getAssetsByCustomerId(@RequestParam UUID customerId) {
        return assetService.getAssetsByCustomerId(customerId);
    }
}
