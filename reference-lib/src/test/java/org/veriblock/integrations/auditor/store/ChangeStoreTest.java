// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.auditor.store;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.auditor.BlockIdentifier;
import org.veriblock.integrations.blockchain.changes.AddBitcoinBlockChange;
import org.veriblock.integrations.blockchain.changes.AddVeriBlockBlockChange;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.integrations.blockchain.store.StoredVeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;

public class ChangeStoreTest {

    @Test
    public void changeStoreStoreBitcoinBTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            AuditorChangesStore changeStore = VeriBlockIntegrationLibraryManager.getContext().getChangeStore();

            Random random = new Random(100L);
            byte[] scratch = new byte[BlockIdentifier.LENGTH];
            random.nextBytes(scratch);

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock oldValue = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.ONE, 0);
            StoredBitcoinBlock newValue = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);

            BlockIdentifier blockIdentifier = BlockIdentifier.wrap(scratch);

            StoredChange storedChange = new StoredChange(blockIdentifier, 0, new AddBitcoinBlockChange(oldValue, newValue));
            changeStore.put(storedChange);
            List<StoredChange> storedChanges = changeStore.get(blockIdentifier);

            Assert.assertTrue(storedChanges != null && storedChanges.size() > 0);
            Assert.assertEquals(storedChange, storedChanges.get(0));
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void changeStoreStoreVeriBTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            AuditorChangesStore changeStore = VeriBlockIntegrationLibraryManager.getContext().getChangeStore();

            Random random = new Random(100L);
            byte[] scratch = new byte[BlockIdentifier.LENGTH];
            random.nextBytes(scratch);

            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock oldValue = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.ONE);
            StoredVeriBlockBlock newValue = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);

            BlockIdentifier blockIdentifier = BlockIdentifier.wrap(scratch);

            StoredChange storedChange = new StoredChange(blockIdentifier, 0, new AddVeriBlockBlockChange(oldValue, newValue));
            changeStore.put(storedChange);
            List<StoredChange> storedChanges = changeStore.get(blockIdentifier);

            Assert.assertTrue(storedChanges != null && storedChanges.size() > 0);
            Assert.assertEquals(storedChange, storedChanges.get(0));
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void nonexistingChangeStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            AuditorChangesStore changeStore = VeriBlockIntegrationLibraryManager.getContext().getChangeStore();
            
            BlockIdentifier blockIdentifier = BlockIdentifier.wrap("123".getBytes());
            List<StoredChange> storedChanges = changeStore.get(blockIdentifier);
            Assert.assertTrue(storedChanges.isEmpty());
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
}