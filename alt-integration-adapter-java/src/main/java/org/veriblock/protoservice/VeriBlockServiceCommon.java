// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import java.util.ArrayList;
import java.util.List;

import org.veriblock.sdk.ValidationResult;

import integration.api.grpc.VeriBlockMessages.GeneralReply;

public class VeriBlockServiceCommon {    
    private VeriBlockServiceCommon() { }
    
    public static GeneralReply validationResultToProto(ValidationResult data) {
        return GeneralReply.newBuilder().setResult(data.isValid()).setResultMessage(data.getMessage()).build();
    }
    
    public static ValidationResult validationResultFromProto(GeneralReply data) {
        if(data.getResult()) {
            return ValidationResult.success();
        }
        return ValidationResult.fail(data.getResultMessage());
    }
    
    public static <T> List<T> nullToEmptyList(List<T> publications) {
        if(publications == null) return new ArrayList<>();
        return publications;
    }
}
