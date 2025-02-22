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

package org.eclipse.dataspaceconnector.spi.observe.policydefinition;


import org.eclipse.dataspaceconnector.spi.observe.Observable;
import org.eclipse.dataspaceconnector.spi.policy.PolicyDefinition;

/**
 * Interface implemented by listeners registered to observe policy definition state changes via {@link Observable#registerListener}.
 * The listener must be called after the state changes are persisted.
 */
public interface PolicyDefinitionListener {

    /**
     * Called after a {@link PolicyDefinition} was created.
     *
     * @param policyDefinition the policyDefinition that has been created.
     */
    default void created(PolicyDefinition policyDefinition) {

    }

    /**
     * Called after a {@link PolicyDefinition} was deleted.
     *
     * @param policyDefinition the policyDefinition that has been deleted.
     */
    default void deleted(PolicyDefinition policyDefinition) {

    }

}
