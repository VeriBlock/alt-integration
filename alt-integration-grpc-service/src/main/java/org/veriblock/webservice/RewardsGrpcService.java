// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import org.veriblock.protoservice.VeriBlockRewardsProtoService;

import integration.api.grpc.RewardsServiceGrpc.RewardsServiceImplBase;
import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.GeneralReply;
import io.grpc.stub.StreamObserver;

public class RewardsGrpcService extends RewardsServiceImplBase {
    
    @Override
    public void resetRewards(VeriBlockMessages.EmptyRequest request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockRewardsProtoService.resetRewards();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void getCalculator(VeriBlockMessages.EmptyRequest request, StreamObserver<VeriBlockMessages.GetCalculatorReply> responseObserver) {  
        VeriBlockMessages.GetCalculatorReply reply = VeriBlockRewardsProtoService.getCalculator();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void rewardsCalculateScore(VeriBlockMessages.RewardsCalculateScoreRequest request, StreamObserver<VeriBlockMessages.RewardsCalculateScoreReply> responseObserver) {
        VeriBlockMessages.RewardsCalculateScoreReply reply = VeriBlockRewardsProtoService.rewardsCalculateScore(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void rewardsCalculateOutputs(VeriBlockMessages.RewardsCalculateOutputsRequest request, StreamObserver<VeriBlockMessages.RewardsCalculateOutputsReply> responseObserver) {
        VeriBlockMessages.RewardsCalculateOutputsReply reply = VeriBlockRewardsProtoService.rewardsCalculateOutputs(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void rewardsCalculatePopDifficulty(VeriBlockMessages.RewardsCalculatePopDifficultyRequest request, StreamObserver<VeriBlockMessages.RewardsCalculateScoreReply> responseObserver) {
        VeriBlockMessages.RewardsCalculateScoreReply reply = VeriBlockRewardsProtoService.rewardsCalculatePopDifficulty(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
