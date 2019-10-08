// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.integrations.AltChainParametersConfig;

public class AltChainParametresConfigProtoConverter {

    private AltChainParametresConfigProtoConverter () {} // never

    public static AltChainParametersConfig fromProto(VeriBlockMessages.AltChainConfigRequest protoData) {
        AltChainParametersConfig config = new AltChainParametersConfig();

        config.keystoneInterval = protoData.getKeystoneInterval();

        return config;
    }

    public static VeriBlockMessages.AltChainConfigRequest toProto(AltChainParametersConfig config) {

        VeriBlockMessages.AltChainConfigRequest.Builder result = VeriBlockMessages.AltChainConfigRequest.newBuilder();

        result.setKeystoneInterval(config.keystoneInterval);
        return result.build();
    }
}
