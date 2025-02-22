/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.transfer.dataplane.sync.validation;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.transfer.dataplane.spi.DataPlaneTransferConstants.CONTRACT_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractValidationRuleTest {

    private final Instant now = Instant.now();
    private final Clock clock = Clock.fixed(now, UTC);

    private final ContractNegotiationStore contractNegotiationStoreMock = mock(ContractNegotiationStore.class);
    private ContractValidationRule rule;


    @BeforeEach
    public void setUp() {
        rule = new ContractValidationRule(contractNegotiationStoreMock, clock);
    }

    @Test
    void shouldSucceedIfContractIsStillValid() {
        var contractId = UUID.randomUUID().toString();
        var contractAgreement = createContractAgreement(contractId, now.plus(1, HOURS));
        when(contractNegotiationStoreMock.findContractAgreement(contractId)).thenReturn(contractAgreement);
        var claims = new JWTClaimsSet.Builder()
                .claim(CONTRACT_ID, contractId)
                .build();

        var result = rule.checkRule(createJwtWith(claims));

        assertThat(result.succeeded()).isTrue();
    }

    @Test
    void shouldFailIfContractIsExpired() {
        var contractId = UUID.randomUUID().toString();
        var contractAgreement = createContractAgreement(contractId, now.minus(1, SECONDS));
        when(contractNegotiationStoreMock.findContractAgreement(contractId)).thenReturn(contractAgreement);
        var claims = new JWTClaimsSet.Builder()
                .claim(CONTRACT_ID, contractId)
                .build();

        var result = rule.checkRule(createJwtWith(claims));

        assertThat(result.failed()).isTrue();
    }

    @Test
    void shouldFailIfContractIdClaimIsMissing() {
        var claims = new JWTClaimsSet.Builder().build();

        var result = rule.checkRule(createJwtWith(claims));

        assertThat(result.succeeded()).isFalse();
    }

    @Test
    void shouldFailIfContractIdContractDoesNotExist() {
        when(contractNegotiationStoreMock.findContractAgreement(any())).thenReturn(null);
        var claims = new JWTClaimsSet.Builder()
                .claim(CONTRACT_ID, "unknownContractId")
                .build();

        var result = rule.checkRule(createJwtWith(claims));

        assertThat(result.succeeded()).isFalse();
    }

    @NotNull
    private SignedJWT createJwtWith(JWTClaimsSet claims) {
        var jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
        return new SignedJWT(jwsHeader, claims);
    }

    private ContractAgreement createContractAgreement(String contractId, Instant endDate) {
        return ContractAgreement.Builder.newInstance()
                .id(contractId)
                .assetId(UUID.randomUUID().toString())
                .policy(Policy.Builder.newInstance().build())
                .contractEndDate(endDate.getEpochSecond())
                .consumerAgentId("consumer-agent-id")
                .providerAgentId("provider-agent-id")
                .build();
    }
}