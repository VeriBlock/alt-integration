// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.sdk.blockchain.store.BlockStore;
import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.blockchain.store.StoredVeriBlockBlock;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GetLastKnownBlocksTest {
    
    private static VeriBlockSecurity security;
    
    @Before
    public void setUp() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        security = veriBlockIntegrationLibraryManager.init();
    }
    
    @After
    public void tearDown() throws SQLException {
        security.shutdown();
    }

    @Test
    public void getLastKnownVBKBlocksTest() throws SQLException {
        byte[] raw1 = Utils.decodeHex("0001998300029690ACA425987B8B529BEC04654A16FCCE708F3F0DEED25E1D2513D05A3B17C49D8B3BCFEFC10CB2E9C4D473B2E25DB7F1BD040098960DE0E313");
        StoredVeriBlockBlock newBlock1 = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw1), BigInteger.TEN);

        byte[] raw2 = Utils.decodeHex("000199840002A69BF9FE9B06E641B61699A9654A16FCCE708F3F0DEED25E1D2513D05A3B7D7F80EB5E94D01C6B3796DDE5647F135DB7F1DD040098960EA12045");
        StoredVeriBlockBlock newBlock2 = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw2), BigInteger.ONE);

        byte[] raw3 = Utils.decodeHex("000199850002461DB458CD6258D3571D4A2A654A16FCCE708F3F0DEED25E1D2513D05A3BB0B8A658CBFFCFBE9185AFDE789841EC5DB7F2360400989610B1662B");
        StoredVeriBlockBlock newBlock3 = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw3), BigInteger.ZERO);

        VeriBlockStore store = Context.getVeriblockStore();

        store.put(newBlock1);
        store.put(newBlock2);
        store.put(newBlock3);
        store.setChainHead(newBlock2);

        List<VBlakeHash> blocks = security.getLastKnownVBKBlocks(16);
        List<VBlakeHash> expectedBlocks = Arrays.asList(newBlock1.getHash(), newBlock2.getHash());
 
        Comparator<VBlakeHash> comparator = Comparator.comparing(VBlakeHash::toString);
        blocks.sort(comparator);
        expectedBlocks.sort(comparator);
        Assert.assertEquals(blocks, expectedBlocks);
    }

    @Test
    public void getLastKnownBTCBlocksTest() throws SQLException {
        BitcoinBlock block1 = new BitcoinBlock(766099456,
                                    Sha256Hash.wrap("00000000000000000004dc9c42c22f489ade54a9349e3a47aee5b55069062afd"),
                                    Sha256Hash.wrap("87839c0e4c6771557ef02a5076c8b46a7157e5532eff7153293791ca852d2e58"),
                                    1572336145, 0x17148edf, 790109764);
        Assert.assertEquals(block1.getHash(),
                            Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"));

        BitcoinBlock block2 = new BitcoinBlock(1073733632,
                                    Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"),
                                    Sha256Hash.wrap("902e5a70c8fa99fb9ba6d0f855f5e84b8ffc3fe56b694889d07031d8adb6a0f8"),
                                    1572336708, 0x17148edf, 344118374);
        Assert.assertEquals(block2.getHash(),
                            Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"));

        BitcoinBlock block3 = new BitcoinBlock(536870912,
                                    Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"),
                                    Sha256Hash.wrap("2dfad61070eeea30ee035cc58ac20a325292802f9445851d14f23b4e71ddee61"),1572337243, 0x17148edf, 2111493782);
        Assert.assertEquals(block3.getHash(),
                            Sha256Hash.wrap("0000000000000000000e008052ab86a7b0c20e46b29c54658b066d471022503f"));

        StoredBitcoinBlock newBlock1 = new StoredBitcoinBlock(block1, BigInteger.TEN, 0);
        StoredBitcoinBlock newBlock2 = new StoredBitcoinBlock(block2, BigInteger.ONE, 0);
        StoredBitcoinBlock newBlock3 = new StoredBitcoinBlock(block3, BigInteger.ZERO, 0);


        BlockStore<StoredBitcoinBlock, Sha256Hash> store = Context.getBitcoinStore();

        store.put(newBlock1);
        store.put(newBlock2);
        store.put(newBlock3);
        store.setChainHead(newBlock2);

        List<Sha256Hash> blocks = security.getLastKnownBTCBlocks(16);
        List<Sha256Hash> expectedBlocks = Arrays.asList(newBlock1.getHash(), newBlock2.getHash());
 
        Comparator<Sha256Hash> comparator = Comparator.comparing(Sha256Hash::toString);
        blocks.sort(comparator);
        expectedBlocks.sort(comparator);
        Assert.assertEquals(blocks, expectedBlocks);
    }
}
