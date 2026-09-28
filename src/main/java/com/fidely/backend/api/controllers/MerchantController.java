package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.merchants.MerchantResponseMapper;
import com.fidely.backend.api.dtos.models.merchants.MerchantResponse;
import com.fidely.backend.api.dtos.models.merchants.UpdateMerchantBrandingRequest;
import com.fidely.backend.api.dtos.models.merchants.UpdateMerchantRequest;
import com.fidely.backend.application.port.in.IMerchantService;
import com.fidely.backend.domain.models.Merchants.Merchant;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/merchants")
public class MerchantController {

    private final IMerchantService merchantService;
    private final MerchantResponseMapper merchantResponseMapper;

    public MerchantController(
            IMerchantService merchantService,
            MerchantResponseMapper merchantResponseMapper
    ) {
        this.merchantService = merchantService;
        this.merchantResponseMapper = merchantResponseMapper;
    }

    @GetMapping(
            path = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MerchantResponse> getCurrentMerchant(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        Merchant merchant = merchantService.getMerchantForManager(userId);

        MerchantResponse response = merchantResponseMapper.toResponse(merchant);

        return ResponseEntity.ok(response);
    }

    @PatchMapping(
            path = "/me",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MerchantResponse> updateCurrentMerchant(
            Authentication authentication,
            @RequestBody UpdateMerchantRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        Merchant merchant = merchantService.updateMerchant(
                userId,
                request.name(),
                request.slug(),
                request.description(),
                request.googleReviewUrl()
        );

        MerchantResponse response = merchantResponseMapper.toResponse(merchant);

        return ResponseEntity.ok(response);
    }

    @PatchMapping(
            path = "/me/branding",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MerchantResponse> updateCurrentMerchantBranding(
            Authentication authentication,
            @RequestBody UpdateMerchantBrandingRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        Merchant merchant = merchantService.updateBranding(
                userId,
                request.logoUrl(),
                request.primaryColor(),
                request.secondaryColor()
        );

        MerchantResponse response = merchantResponseMapper.toResponse(merchant);

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            path = "/{id}/public",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MerchantResponse> getPublicMerchant(
            @PathVariable UUID id
    ) {
        Merchant merchant = merchantService.getPublicMerchant(id);

        MerchantResponse response = merchantResponseMapper.toResponse(merchant);

        return ResponseEntity.ok(response);
    }
}