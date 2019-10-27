// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.integrations.sqlite.tables.KeyValueData;
import org.veriblock.integrations.sqlite.tables.BitcoinBlockRepository;
import org.veriblock.integrations.sqlite.tables.KeyValueRepository;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.Sha256Hash;

//FIXME: split this into tests of BitcoinBlockRepository and KeyValueRepository
public class SqliteBitcoinBlocksTableTest {
    
    private static final String databasePath = Paths.get(FileManager.getTempDirectory(), ConnectionSelector.defaultDatabaseName).toString();
    private static Connection connection;
    private static BitcoinBlockRepository bitcoinBlocks;
    private static KeyValueRepository keyValue;
    private static final byte[] rawBlock = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
    private static final StoredBitcoinBlock newBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(rawBlock), BigInteger.TEN, 0);

    @Before
    public void setUp() throws SQLException {
        connection = ConnectionSelector.setConnection(databasePath);
        bitcoinBlocks = new BitcoinBlockRepository(connection);
        keyValue = new KeyValueRepository(connection);
    }
    
    @After
    public void tearDown() throws IOException, SQLException {
        bitcoinBlocks.clear();
        keyValue.clear();
        if(connection != null) connection.close();
    }

    @Test
    public void createBlockTest() throws IOException, SQLException {
        bitcoinBlocks.clear();
        List<StoredBitcoinBlock> blocks = bitcoinBlocks.getAll();
        Assert.assertEquals(blocks.size(), 0);

        bitcoinBlocks.save(newBlock);
        blocks = bitcoinBlocks.getAll();
        Assert.assertEquals(blocks.size(), 1);
    }
    
    @Test
    public void createKVTest() throws IOException, SQLException {
        keyValue.clear();
        
        KeyValueData value = new KeyValueData();
        value.key = "1";
        value.value = "test";
        keyValue.save(value.key, value.value);
        String storedValue = keyValue.getValue(value.key);
        Assert.assertEquals(storedValue, value.value);
    }
    
    @Test
    public void deleteTest() throws IOException, SQLException {
        bitcoinBlocks.clear();
        
        bitcoinBlocks.save(newBlock);
        
        List<StoredBitcoinBlock> blocks = bitcoinBlocks.getAll();
        Assert.assertEquals(blocks.size(), 1);
        
        bitcoinBlocks.delete(newBlock.getHash());
        blocks = bitcoinBlocks.getAll();
        Assert.assertEquals(blocks.size(), 0);
        
        // try to delete non existing recorss. See that nothing happens.
        bitcoinBlocks.delete(newBlock.getHash());
    }
}
