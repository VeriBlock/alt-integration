// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.sqlite.tables;

import org.veriblock.sdk.models.AltChainBlock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ContainRepository {
    private Connection connectionSource;

    public static final String tableName = "contain";
    public static final String txHashColumnName = "tx_hash";
    public static final String blockHashColumnName = "block_hash";
    public static final String blockHeightColumnName = "block_height";
    public static final String blockTimestampColumnName = "block_timestamp";

    public ContainRepository(Connection connection) throws SQLException {
        this.connectionSource = connection;

        try(Statement stmt = connectionSource.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName
                    + "(\n "
                    + txHashColumnName + " TEXT NOT NULL,\n "
                    + blockHashColumnName + " TEXT NOT NULL,\n "
                    + blockHeightColumnName + " INT NOT NULL,\n "
                    + blockTimestampColumnName + " INT NOT NULL,\n "
                    + " PRIMARY KEY (" + txHashColumnName + ", " + blockHashColumnName + ")\n "
                    + " FOREIGN KEY (" + txHashColumnName + ")\n "
                    + " REFERENCES " + PoPTransactionsRepository.tableName + " (" + PoPTransactionsRepository.txHashColumnName + ")\n "
                    + ");");
        }

        try(Statement stmt = connectionSource.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
        }
    }

    public void clear() throws SQLException {
        Statement stmt = null;
        try{
            stmt = connectionSource.createStatement();
            stmt.execute( "DELETE FROM " + tableName);
        }
        finally {
            if(stmt != null) {
                stmt.close();
            }
        }
    }

    public void save(String txHash, AltChainBlock containingBlock) throws SQLException {
        String sql = "REPLACE INTO contain (tx_hash, block_hash, block_height, block_timestamp) " +
                "VALUES(?, ?, ?, ?)";
        try(PreparedStatement stmt = connectionSource.prepareStatement(sql)) {
            int i = 0;
            stmt.setObject(++i, txHash);
            stmt.setObject(++i, containingBlock.getHash());
            stmt.setLong(++i, containingBlock.getHeight());
            stmt.setLong(++i, containingBlock.getTimestamp());
            stmt.execute();
        }
    }
}
