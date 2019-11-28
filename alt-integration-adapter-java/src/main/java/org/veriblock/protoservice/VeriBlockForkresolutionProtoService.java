// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.forkresolution.ForkresolutionComparator;
import org.veriblock.protoconverters.AltChainBlockProtoConverter;
import org.veriblock.sdk.AltChainBlock;
import org.veriblock.sdk.ValidationResult;

import integration.api.grpc.VeriBlockMessages;

public class VeriBlockForkresolutionProtoService {

    private static final Logger log = LoggerFactory.getLogger(VeriBlockForkresolutionProtoService.class);

    private VeriBlockForkresolutionProtoService() {}

    public static VeriBlockMessages.CompareReply compareTwoBranches(VeriBlockMessages.TwoBranchesRequest request)
    {
        ValidationResult validationResult = null;
        List<AltChainBlock> leftFork = AltChainBlockProtoConverter.fromProto(request.getLeftForkList());
        List<AltChainBlock> rightFork = AltChainBlockProtoConverter.fromProto(request.getRightForkList());
        int compareResult = 0;
        try {
             compareResult = ForkresolutionComparator.compareTwoBranches(leftFork, rightFork);
             validationResult = ValidationResult.success();
        }
        catch (SQLException e)
        {
            validationResult = ValidationResult.fail(e.getMessage());
            log.debug("Could not call VeriBlock forkresolution", e);
        }

        VeriBlockMessages.CompareReply.Builder reply = VeriBlockMessages.CompareReply.newBuilder();

        return reply.setComparingsResult(compareResult).
                setResult(VeriBlockServiceCommon.validationResultToProto(validationResult)).
                build();
    }
}
