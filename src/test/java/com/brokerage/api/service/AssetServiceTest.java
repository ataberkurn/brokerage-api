package com.brokerage.api.service;

import com.brokerage.api.entity.Asset;
import com.brokerage.api.entity.Customer;
import com.brokerage.api.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private AssetService assetService;

    private Asset asset;
    private UUID customerId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        asset = new Asset();
        asset.setCustomer(customer);
        asset.setId(UUID.randomUUID());
        asset.setName("ABC");
    }

    @Test
    public void testSave() {
        when(assetRepository.save(asset)).thenReturn(asset);

        Asset savedAsset = assetService.save(asset);

        assertNotNull(savedAsset);
        assertEquals(asset.getId(), savedAsset.getId());
        verify(assetRepository, times(1)).save(asset);
    }

    @Test
    public void testGetAssetByCustomerIdAndName_Exists() {
        String name = "ABC";
        when(assetRepository.findByCustomer_IdAndName(customerId, name)).thenReturn(Optional.of(asset));

        Asset foundAsset = assetService.getAssetByCustomerIdAndName(customerId, name);

        assertNotNull(foundAsset);
        assertEquals(asset.getId(), foundAsset.getId());
        verify(assetRepository, times(1)).findByCustomer_IdAndName(customerId, name);
    }

    @Test
    public void testGetAssetByCustomerIdAndName_NotFound() {
        String name = "ABC";
        when(assetRepository.findByCustomer_IdAndName(customerId, name)).thenReturn(Optional.empty());

        Asset foundAsset = assetService.getAssetByCustomerIdAndName(customerId, name);

        assertNull(foundAsset);
        verify(assetRepository, times(1)).findByCustomer_IdAndName(customerId, name);
    }

    @Test
    public void testGetAssetsByCustomerId() {
        List<Asset> assets = Arrays.asList(asset);
        when(assetRepository.findAllByCustomer_Id(customerId)).thenReturn(assets);

        List<Asset> foundAssets = assetService.getAssetsByCustomerId(customerId);

        assertNotNull(foundAssets);
        assertEquals(1, foundAssets.size());
        assertEquals(asset.getId(), foundAssets.get(0).getId());
        verify(assetRepository, times(1)).findAllByCustomer_Id(customerId);
    }
}
