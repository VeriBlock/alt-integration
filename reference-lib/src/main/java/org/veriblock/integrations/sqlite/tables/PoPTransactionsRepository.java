// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class PoPTransactionsRepository {

    private Connection connectionSource;

    public static final String tableName = "PoPTransactions";
    public static final String txHashColumnName = "txHash";
    public static final String endorsedBlockHashColumnName = "endorsedBlockHash";
    public static final String altPublicationHashColumnName = "AltPublicationHash";

    public PoPTransactionsRepository(Connection connection) throws SQLException
    {
        this.connectionSource = connection;

        Statement stmt = null;
        try{
            stmt = connectionSource.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName
                    + "(\n "
                    + txHashColumnName + " TEXT PRIMARY KEY,\n "
                    + endorsedBlockHashColumnName + " TEXT NOT NULL,\n "
                    + altPublicationHashColumnName + " TEXT NOT NULL,\n "
                    + " FOREIGN KEY (" + altPublicationHashColumnName + ")\n "
                    + " REFERENCES " + AltPublicationRepository.tableName + " (" + AltPublicationRepository.altPublicationHash + ")\n "
                    + ");");
        }
        finally{
            if(stmt != null) stmt.close();
            stmt = null;
        }

        try {
            stmt = connectionSource.createStatement();
            stmt.execute("PRAGMA journal_mode=WAL;");
        } finally {
            if(stmt != null) stmt.close();
        }
    }

    public void clear() throws SQLException
    {
        Statement stmt = null;
        try{
            stmt = connectionSource.createStatement();
            stmt.execute( "DELETE FROM " + tableName);
        }
        finally {
            if(stmt != null) stmt.close();
        }
    }

    public void save(String txHash, String endoresedBlockHash, String altPublicationIndex) throws SQLException
    {
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("REPLACE INTO " + tableName + " ('" + txHashColumnName + "', '" + endorsedBlockHashColumnName + "', '" + altPublicationHashColumnName + "') " +
                    "VALUES(?, ?, ?)");
            int i = 0;
            stmt.setObject(++i, txHash);
            stmt.setObject(++i, endoresedBlockHash);
            stmt.setObject(++i, altPublicationIndex);
            stmt.execute();
        } finally {
            if(stmt != null) stmt.close();
        }
    }
}
