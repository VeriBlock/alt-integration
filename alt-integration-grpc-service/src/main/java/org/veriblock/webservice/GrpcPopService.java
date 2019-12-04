package org.veriblock.webservice;

import integration.api.grpc.PopServiceGrpc.PopServiceImplBase;
import integration.api.grpc.VeriBlockMessages;
import org.veriblock.protoservice.PopServiceProto;
import io.grpc.stub.StreamObserver;
import org.veriblock.sdk.VeriBlockSecurity;

public class GrpcPopService extends PopServiceImplBase {

    public GrpcPopService(VeriBlockSecurity security) {
        super();
        PopServiceProto.setVeriBlockSecurity(security);
    }

    @Override
    public void checkATVInternally(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.CheckReply> responseObserver)
    {
        try {
            VeriBlockMessages.CheckReply reply = PopServiceProto.checkATVInternally(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void checkVTBInternally(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.CheckReply> responseObserver)
    {
        try {
            VeriBlockMessages.CheckReply reply = PopServiceProto.checkVTBInternally(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void rewardsCalculateOutputs(VeriBlockMessages.RewardsCalculateRequest request, StreamObserver<VeriBlockMessages.RewardsOutputsReply> responseObserver)
    {
        try {
            VeriBlockMessages.RewardsOutputsReply reply = PopServiceProto.rewardsCalculateOutputs(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
