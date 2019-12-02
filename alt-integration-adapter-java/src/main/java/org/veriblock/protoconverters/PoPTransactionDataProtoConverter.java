package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;

import java.util.List;

public class PoPTransactionDataProtoConverter {

    private PoPTransactionDataProtoConverter () {} // private

    public static PoPTransactionData fromProto (VeriBlockMessages.PoPTransactionData protoData) {
        AltPublication altPublication = AltPublicationProtoConverter.fromProto(protoData.getAltPublication());
        List<VeriBlockPublication> veriBlockPublications = VeriBlockPublicationProtoConverter.fromProto(protoData.getVeriblockPublicationsList());

        return new PoPTransactionData(protoData.getHash(), altPublication, veriBlockPublications);
    }

    public static VeriBlockMessages.PoPTransactionData toProto(PoPTransactionData popTx) {
        VeriBlockMessages.PoPTransactionData.Builder builder = VeriBlockMessages.PoPTransactionData.newBuilder();
        builder.setHash(popTx.txHash)
                .setAltPublication(AltPublicationProtoConverter.toProto(popTx.altPublication))
                .addAllVeriblockPublications(VeriBlockPublicationProtoConverter.toProto(popTx.veriBlockPublications));

        return builder.build();
    }
}
