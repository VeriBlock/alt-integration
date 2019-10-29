// Bitcoin Blockchain Project
// Copyright 2017-2018 Bitcoin, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite.tables;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.sdk.services.SerializeDeserializeService;

public class BitcoinBlockRepositoryTest {
    private byte[] rawBlock;
    private StoredBitcoinBlock newBlock;

    private Connection connection;
    private BitcoinBlockRepository repo;

    @Before
    public void init() throws SQLException {
        rawBlock = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        newBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(rawBlock), BigInteger.TEN, 0);

        connection = ConnectionSelector.setConnectionDefault();
        repo = new BitcoinBlockRepository(connection);
        repo.clear();
    }

    @After
    public void closeConnection() throws SQLException {
        if (connection != null)
            connection.close();
    }
    
    @Test
    public void GetNonexistentBlockTest() throws SQLException, IOException {
        StoredBitcoinBlock block = repo.get(newBlock.getHash());
        Assert.assertNull(block);
    }

    @Test
    public void DeleteNonexistentBlockTest() throws SQLException, IOException {
        repo.delete(newBlock.getHash());
    }

    @Test
    public void DeleteBlockTest() throws SQLException, IOException {
        repo.save(newBlock);
        repo.delete(newBlock.getHash());

        StoredBitcoinBlock block = repo.get(newBlock.getHash());
        Assert.assertNull(block);
    }

    @Test
    public void AddBlockTest() throws SQLException, IOException {
        repo.save(newBlock);

        StoredBitcoinBlock block = repo.get(newBlock.getHash());
        Assert.assertEquals(block, newBlock);
    }

    //FIXME: test getAll()
    //FIXME: test both get() and getEndsWithId()
    //FIXME: test clear() explicitly
}
