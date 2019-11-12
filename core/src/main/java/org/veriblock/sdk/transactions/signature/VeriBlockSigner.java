// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.transactions.signature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.math.ec.FixedPointUtil;
import org.veriblock.sdk.Sha256Hash;

public class VeriBlockSigner {
    
    // The parameters of the secp256k1 curve that Bitcoin uses.
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

    // The parameters of the secp256k1 curve that Bitcoin uses.
    public static final ECDomainParameters CURVE;

    public static final BigInteger HALF_CURVE_ORDER;

    static {
        // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations.
        FixedPointUtil.precompute(CURVE_PARAMS.getG());
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
                CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    }
    
    // Returns true if the S component is "low", that means it is below {@link ECKey#HALF_CURVE_ORDER}. See <a
    // href="https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures">BIP62</a>.
    public static boolean isCanonical(BigInteger s) {
        return s.compareTo(HALF_CURVE_ORDER) <= 0;
    }

    // Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
    // This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
    // the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
    // been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
    // considered legal and the other will be banned.
    public static BigInteger toCanonicalised(BigInteger s) {
        if (isCanonical(s)) return s;
        
        // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
        // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
        //    N = 10
        //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
        //    10 - 8 == 2, giving us always the latter solution, which is canonical.
        return CURVE.getN().subtract(s);
    }

    // generates compatible with Bitcoin BIP62 requirements signature
    public static byte[] signMessageWithPrivateKey(byte[] message, PrivateKey privateKey) throws SignatureException {
        if (message == null) {
            throw new SignatureException("signMessageWithPrivateKey cannot be called with a null message to sign!");
        }

        if (privateKey == null) {
            throw new SignatureException("signMessageWithPrivateKey cannot be called with a null private key!");
        }

        try {
            //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            ECPrivateKeyParameters privKey = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(privateKey);
            signer.init(true, privKey);
            
            byte[] hash = Sha256Hash.hash(message);
            // generate r, s
            BigInteger[] components = signer.generateSignature(hash);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(components[0]));
            ///HACK: here we convert to the 'LOW_S' type of signature
            seq.addObject(new ASN1Integer(toCanonicalised(components[1])));
            seq.close();
            
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SignatureException(e);
        } catch (InvalidKeyException e) {
            throw new SignatureException(e);
        }
    }
}
