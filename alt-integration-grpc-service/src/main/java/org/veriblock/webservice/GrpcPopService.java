package org.veriblock.webservice;

import integration.api.grpc.GrpcPopServiceGrpc.GrpcPopServiceImplBase;
import integration.api.grpc.VeriBlockMessages;
import org.veriblock.protoservice.PopServiceProto;
import io.grpc.stub.StreamObserver;
import org.veriblock.sdk.VeriBlockSecurity;

public class GrpcPopService extends GrpcPopServiceImplBase {

    public GrpcPopService(VeriBlockSecurity security) {
        super();
        PopServiceProto.setVeriBlockSecurity(security);
    }

    @Override
    public void checkATVInternally(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.CheckReply> responseObserver) {
        try {
            VeriBlockMessages.CheckReply reply = PopServiceProto.checkATVInternally(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void checkVTBInternally(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.CheckReply> responseObserver) {
        try {
            VeriBlockMessages.CheckReply reply = PopServiceProto.checkVTBInternally(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void rewardsCalculateOutputs(VeriBlockMessages.RewardsCalculateRequest request, StreamObserver<VeriBlockMessages.RewardsCalculateReply> responseObserver) {
        try {
            VeriBlockMessages.RewardsCalculateReply reply = PopServiceProto.rewardsCalculateOutputs(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void saveBlockPopTxToDatabase(VeriBlockMessages.SaveBlockPopTxRequest request, StreamObserver<VeriBlockMessages.EmptyReply> responseObserver) {
        try {
            VeriBlockMessages.EmptyReply reply = PopServiceProto.saveBlockPopTxToDatabase(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateContext(VeriBlockMessages.UpdateContextRequest request, StreamObserver<VeriBlockMessages.EmptyReply> responseObserver) {
        try {
            VeriBlockMessages.EmptyReply reply = PopServiceProto.updateContext(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void compareTwoBranches(VeriBlockMessages.TwoBranchesRequest request, StreamObserver<VeriBlockMessages.CompareTwoBranchesReply> responseObserver) {
        try {
            VeriBlockMessages.CompareTwoBranchesReply reply = PopServiceProto.compareTwoBranches(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getLastKnownVBKBlocks(VeriBlockMessages.GetLastKnownBlocksRequest request, StreamObserver<VeriBlockMessages.GetLastKnownBlocksReply> responseObserver) {
        try {
            VeriBlockMessages.GetLastKnownBlocksReply reply = PopServiceProto.getLastKnownVBKBlocks(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getLastKnownBTCBlocks(VeriBlockMessages.GetLastKnownBlocksRequest request, StreamObserver<VeriBlockMessages.GetLastKnownBlocksReply> responseObserver) {
        try {
            VeriBlockMessages.GetLastKnownBlocksReply reply = PopServiceProto.getLastKnownBTCBlocks(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void parseAltPublication(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.AltPublication> responseObserver) {
        try {
            VeriBlockMessages.AltPublication reply = PopServiceProto.parseAltPublication(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void parseVeriBlockPublication(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockPublication> responseObserver) {
        try {
            VeriBlockMessages.VeriBlockPublication reply = PopServiceProto.parseVeriBlockPublication(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getPublicationDataFromAltPublication(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.PublicationData> responseObserve) {
        try {
            VeriBlockMessages.PublicationData reply = PopServiceProto.getPublicationDataFromAltPublication(request);
            responseObserve.onNext(reply);
            responseObserve.onCompleted();
        } catch (Exception e) {
            responseObserve.onError(e);
        }
    }

    @Override
    public void addPayloads(VeriBlockMessages.AddPayloadsDataRequest request, StreamObserver<VeriBlockMessages.EmptyReply> responseObserve) {
        try {
            VeriBlockMessages.EmptyReply reply = PopServiceProto.addPayloads(request);
            responseObserve.onNext(reply);
            responseObserve.onCompleted();
        } catch (Exception e) {
            responseObserve.onError(e);
        }
    }

    @Override
    public void removePayloads(VeriBlockMessages.RemovePayloadsRequest request, StreamObserver<VeriBlockMessages.EmptyReply> responseObserve) {
        try {
            VeriBlockMessages.EmptyReply reply = PopServiceProto.removePayloads(request);
            responseObserve.onNext(reply);
            responseObserve.onCompleted();
        } catch (Exception e) {
            responseObserve.onError(e);
        }
    }
}