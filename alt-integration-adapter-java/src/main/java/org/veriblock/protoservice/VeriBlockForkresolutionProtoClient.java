// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import integration.api.grpc.ForkresolutionServiceGrpc;
import integration.api.grpc.ForkresolutionServiceGrpc.ForkresolutionServiceBlockingStub;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.Channel;
import org.veriblock.protoconverters.AltChainBlockProtoConverter;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.Pair;
import org.veriblock.sdk.models.ValidationResult;

import java.util.List;

public class VeriBlockForkresolutionProtoClient {

    private final ForkresolutionServiceBlockingStub service;

    public VeriBlockForkresolutionProtoClient(Channel channel) { service = ForkresolutionServiceGrpc.newBlockingStub(channel); }

    public Pair<ValidationResult, Integer> compareTwoBranches(List<AltChainBlock> leftFork, List<AltChainBlock> rightFork)
    {
        VeriBlockMessages.TwoBranchesRequest request = VeriBlockMessages.TwoBranchesRequest.newBuilder()
                .addAllLeftFork(AltChainBlockProtoConverter.toProto(leftFork))
                .addAllRightFork(AltChainBlockProtoConverter.toProto(rightFork))
                .build();
        VeriBlockMessages.CompareReply reply = service.compareTwoBranches(request);

        ValidationResult validationResult = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());

        return new Pair<>(validationResult, reply.getComparingsResult());
    }

}
