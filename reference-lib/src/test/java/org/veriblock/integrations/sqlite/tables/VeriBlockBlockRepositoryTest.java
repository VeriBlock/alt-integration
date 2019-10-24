// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain.store;

import org.junit.*;

import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.tables.VeriBlockBlockRepository;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class VeriBlockBlockRepositoryTest {
    private byte[] rawBlock;
    private StoredVeriBlockBlock newBlock;

    private Connection connection;
    private VeriBlockBlockRepository repo;

    @Before
    public void init() throws SQLException {
        rawBlock = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
        newBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(rawBlock), BigInteger.TEN);

        connection = ConnectionSelector.setConnectionDefault();
        repo = new VeriBlockBlockRepository(connection);
        repo.clear();
    }

    @After
    public void closeConnection() throws SQLException {
        if (connection != null)
            connection.close();
    }
    
    @Test
    public void GetNonexistentBlockTest() throws SQLException, IOException {
        List<StoredVeriBlockBlock> blocks = repo.getEndsWithId(newBlock.getHash());
        Assert.assertTrue(blocks.isEmpty());
    }

    @Test
    public void DeleteNonexistentBlockTest() throws SQLException, IOException {
        repo.delete(newBlock.getHash());
    }

    @Test
    public void DeleteBlockTest() throws SQLException, IOException {
        repo.save(newBlock);
        repo.delete(newBlock.getHash());

        List<StoredVeriBlockBlock> blocks = repo.getEndsWithId(newBlock.getHash());
        Assert.assertTrue(blocks.isEmpty());
    }

    @Test
    public void AddBlockTest() throws SQLException, IOException {
        repo.save(newBlock);
        List<StoredVeriBlockBlock> blocks = repo.getEndsWithId(newBlock.getHash());
        Assert.assertFalse(blocks.isEmpty());
        Assert.assertEquals(blocks.get(0), newBlock);
    }

    @Test
    public void AddBlockWithBlockOfProofTest() throws SQLException, IOException {
        Sha256Hash blockOfProof = Sha256Hash.wrap("00000000000000b345b7bbf29bda1507a679b97967f99a10ab0088899529def7");
        newBlock.setBlockOfProof(blockOfProof);

        repo.save(newBlock);
        List<StoredVeriBlockBlock> blocks = repo.getEndsWithId(newBlock.getHash());
        Assert.assertFalse(blocks.isEmpty());
        Assert.assertEquals(blocks.get(0), newBlock);
    }

    //FIXME: test getAll()
    //FIXME: test getEndsWithId() using trimmed VBlakeHash
    //FIXME: test clear() explicitly
}
