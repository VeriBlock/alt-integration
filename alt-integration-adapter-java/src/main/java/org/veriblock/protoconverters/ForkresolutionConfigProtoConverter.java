// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.integrations.forkresolution.ForkresolutionConfig;

public class ForkresolutionConfigProtoConverter {

    private ForkresolutionConfigProtoConverter() {} // never

    public static ForkresolutionConfig fromProto(VeriBlockMessages.ForkresolutionConfigRequest protoData) {
        ForkresolutionConfig config = new ForkresolutionConfig(protoData.getKeystoneFinalityDelay(), protoData.getAmnestyPeriod());
        return config;
    }

    public static VeriBlockMessages.ForkresolutionConfigRequest toProto(ForkresolutionConfig config) {
        VeriBlockMessages.ForkresolutionConfigRequest.Builder result = VeriBlockMessages.ForkresolutionConfigRequest.newBuilder();

        result.setKeystoneFinalityDelay(config.keystoneFinalityDelay);
        result.setAmnestyPeriod(config.amnestyPeriod);

        return result.build();
    }

}
