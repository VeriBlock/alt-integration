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
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.sqlite.tables.KeyValueData;
import org.veriblock.integrations.sqlite.tables.BitcoinBlocksRepository;
import org.veriblock.integrations.sqlite.tables.BlockData;
import org.veriblock.integrations.sqlite.tables.KeyValueRepository;
import org.veriblock.sdk.Sha256Hash;

public class SqliteBitcoinBlocksTableTest {
    
    private static final String databasePath = Paths.get(FileManager.getTempDirectory(), ConnectionSelector.defaultDatabaseName).toString();
    private static Connection connection;
    private static BitcoinBlocksRepository bitcoinBlocks;
    private static KeyValueRepository keyValue;
    
    @Before
    public void setUp() throws SQLException {
        connection = ConnectionSelector.setConnection(databasePath);
        bitcoinBlocks = new BitcoinBlocksRepository(connection);
        keyValue = new KeyValueRepository(connection);
    }
    
    @After
    public void tearDown() throws IOException, SQLException {
        bitcoinBlocks.getBlocksRepository().clear();
        keyValue.clear();
        if(connection != null) connection.close();
    }

    @Test
    public void createBlockTest() throws IOException, SQLException {
        bitcoinBlocks.getBlocksRepository().clear();
        List<BlockData> blocks = bitcoinBlocks.getBlocksRepository().getAll();
        Assert.assertTrue(blocks.size() == 0);

        BlockData newBlock = new BlockData();
        newBlock.height = 0;
        newBlock.id = Sha256Hash.ZERO_HASH.toString();
        newBlock.previousId = Sha256Hash.ZERO_HASH.toString();
        newBlock.work = BigInteger.ZERO;
        newBlock.data = new byte[0];
        bitcoinBlocks.getBlocksRepository().save(newBlock);
        blocks = bitcoinBlocks.getBlocksRepository().getAll();
        Assert.assertTrue(blocks.size() == 1);
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
        bitcoinBlocks.getBlocksRepository().clear();
        
        BlockData newBlock = new BlockData();
        newBlock.height = 0;
        newBlock.id = Sha256Hash.ZERO_HASH.toString();
        newBlock.previousId = Sha256Hash.ZERO_HASH.toString();
        newBlock.work = BigInteger.ZERO;
        newBlock.data = new byte[0];
        bitcoinBlocks.getBlocksRepository().save(newBlock);
        
        List<BlockData> blocks = bitcoinBlocks.getBlocksRepository().getAll();
        Assert.assertTrue(blocks.size() == 1);
        
        bitcoinBlocks.getBlocksRepository().delete(newBlock.id);
        blocks = bitcoinBlocks.getBlocksRepository().getAll();
        Assert.assertTrue(blocks.size() == 0);
        
        // try to delete non existing recors. See that nothing happens.
        bitcoinBlocks.getBlocksRepository().delete(newBlock.id);
    }
}