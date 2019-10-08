// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import integration.api.grpc.ForkresolutionServiceGrpc.ForkresolutionServiceImplBase;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.stub.StreamObserver;
import org.veriblock.protoservice.VeriBlockForkresolutionProtoService;

public class ForkresolutionGrpcService extends ForkresolutionServiceImplBase {

    @Override
    public void compareTwoBranches(VeriBlockMessages.TwoBranchesRequest request, StreamObserver<VeriBlockMessages.CompareReply> responseObserver) {
        VeriBlockMessages.CompareReply reply = VeriBlockForkresolutionProtoService.compareTwoBranches(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void setForkresolutionConfig(VeriBlockMessages.ForkresolutionConfigRequest request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = VeriBlockForkresolutionProtoService.setForkresolutionConfig(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
