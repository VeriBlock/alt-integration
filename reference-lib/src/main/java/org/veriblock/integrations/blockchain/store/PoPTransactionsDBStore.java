// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.tables.*;
import org.veriblock.sdk.AltChainBlock;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PoPTransactionsDBStore {
    private static final Logger log = LoggerFactory.getLogger(AuditorChangesStore.class);

    private Connection connectionResource;

    private PoPTransactionsRepository popTxRepo;
    private ContainRepository containRepo;
    private AltPublicationRepository altPublicationRepo;
    private VeriBlockPublicationRepository veriBlockPublicationRepo;
    private PoPTransactionsVeriblockPublicationRefRepository popTxVeriBlockPublicationRefRepo;

    public PoPTransactionsDBStore(String databasePath) throws SQLException
    {
        this.connectionResource = ConnectionSelector.setConnection(databasePath);

        popTxRepo = new PoPTransactionsRepository(connectionResource);
        containRepo = new ContainRepository(connectionResource);
        altPublicationRepo = new AltPublicationRepository(connectionResource);
        veriBlockPublicationRepo = new VeriBlockPublicationRepository(connectionResource);
        popTxVeriBlockPublicationRefRepo = new PoPTransactionsVeriblockPublicationRefRepository(connectionResource);
    }

    public PoPTransactionsDBStore() throws SQLException
    {
        this.connectionResource = ConnectionSelector.setConnectionDefault();
        popTxRepo = new PoPTransactionsRepository(connectionResource);
        containRepo = new ContainRepository(connectionResource);
        altPublicationRepo = new AltPublicationRepository(connectionResource);
        veriBlockPublicationRepo = new VeriBlockPublicationRepository(connectionResource);
        popTxVeriBlockPublicationRefRepo = new PoPTransactionsVeriblockPublicationRefRepository(connectionResource);
    }

    public void clear() throws SQLException
    {
        popTxRepo.clear();
        containRepo.clear();
        altPublicationRepo.clear();
        veriBlockPublicationRepo.clear();
        popTxVeriBlockPublicationRefRepo.clear();
    }

    public void shutdown()
    {
        try {
            if(connectionResource != null) connectionResource.close();
        } catch (SQLException e) {
            log.debug("Error closing database connection", e);
        }
    }

    public void addPoPTransaction(PoPTransactionData popTx, AltChainBlock containingBlock, AltChainBlock endorsedBlock) throws SQLException
    {
        int altPublicationIndex = altPublicationRepo.save(popTx.altPublication);

        popTxRepo.save(popTx.txHash, endorsedBlock.getHash(), altPublicationIndex);
        containRepo.save(popTx.txHash, containingBlock.getHash());

        for(VeriBlockPublication publication: popTx.veriBlockPublications)
        {
            int veriBlockPublicationIndex = veriBlockPublicationRepo.save(publication);
            popTxVeriBlockPublicationRefRepo.save(popTx.txHash, veriBlockPublicationIndex);
        }
    }

    public List<AltPublication> getAltPublciationsEndorse(AltChainBlock endorsedBlock, List<AltChainBlock> containBlocks) throws SQLException
    {
        List<AltPublication> resultData = new ArrayList<AltPublication>();

        PreparedStatement stmt = null;
        try{
            StringBuilder sql = new StringBuilder("SELECT " +  AltPublicationRepository.tableName + "." + AltPublicationRepository.altPublicationColumnName +
                    " FROM " + PoPTransactionsRepository.tableName + " LEFT JOIN " + AltPublicationRepository.tableName +
                    " ON " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.altPublicationIdColumnName +
                    " = " + AltPublicationRepository.tableName + "." + AltPublicationRepository.idColumnName +
                    " LEFT JOIN " + ContainRepository.tableName +
                    " ON " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.txHashColumnName +
                    " = " + ContainRepository.tableName + "." + ContainRepository.txHashColumnName +
                    " WHERE " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.endorsedBlockHashColumnName + " = '" + endorsedBlock.getHash() +"'" +
                    " AND " + ContainRepository.tableName + "." + ContainRepository.blockHashColumnName + " IN (");
            for (int i = 0; i < containBlocks.size(); i++) {
                sql.append("?,");
            }
            sql.delete(sql.length()-1, sql.length());
            sql.append(")");

            stmt = connectionResource.prepareStatement(sql.toString());
            for(int i = 0; i < containBlocks.size(); i++)
            {
                stmt.setString(i + 1, containBlocks.get(i).getHash());
            }

            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next())
            {
                resultData.add(SerializeDeserializeService.parseAltPublication(resultSet.getBytes(AltPublicationRepository.altPublicationColumnName)));
            }
        }
        finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }

        return resultData;
    }

    public List<AltPublication> getAltPublicationsFromBlock(AltChainBlock block) throws SQLException
    {
        List<AltPublication> resultData = new ArrayList<AltPublication>();

        PreparedStatement stmt = null;
        try{
            stmt = connectionResource.prepareStatement( " SELECT " + AltPublicationRepository.tableName + "." + AltPublicationRepository.altPublicationColumnName +
                    " FROM " + PoPTransactionsRepository.tableName + " LEFT JOIN " + AltPublicationRepository.tableName +
                    " ON " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.altPublicationIdColumnName +
                    " = " + AltPublicationRepository.tableName + "." + AltPublicationRepository.idColumnName +
                    " LEFT JOIN " + ContainRepository.tableName +
                    " ON " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.txHashColumnName +
                    " = " + ContainRepository.tableName + "." + ContainRepository.txHashColumnName +
                    " WHERE " + ContainRepository.tableName + "." + ContainRepository.blockHashColumnName + " = '" + block.getHash() +"'");

            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next())
            {
                resultData.add(SerializeDeserializeService.parseAltPublication(resultSet.getBytes(AltPublicationRepository.altPublicationColumnName)));
            }
        }
        finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }

        return resultData;
    }

    public List<VeriBlockPublication> getVeriBlockPublicationsFromBlock(AltChainBlock block) throws SQLException
    {
        List<VeriBlockPublication> resultData = new ArrayList<VeriBlockPublication>();

        PreparedStatement stmt = null;
        try{
            stmt = connectionResource.prepareStatement( " SELECT " + VeriBlockPublicationRepository.tableName + "." + VeriBlockPublicationRepository.veriBlockPublicationColumnName +
                    " FROM " + VeriBlockPublicationRepository.tableName + " LEFT JOIN " + PoPTransactionsVeriblockPublicationRefRepository.tableName +
                    " ON " + PoPTransactionsVeriblockPublicationRefRepository.tableName + "." + PoPTransactionsVeriblockPublicationRefRepository.veriBlockPublciationIdColumnName +
                    " = " + VeriBlockPublicationRepository.tableName + "." + VeriBlockPublicationRepository.idColumnName +
                    " LEFT JOIN " + ContainRepository.tableName +
                    " ON " + PoPTransactionsVeriblockPublicationRefRepository.tableName + "." + PoPTransactionsVeriblockPublicationRefRepository.txHashColumnName +
                    " = " + ContainRepository.tableName + "." + ContainRepository.txHashColumnName +
                    " WHERE " + ContainRepository.tableName + "." + ContainRepository.blockHashColumnName + " = '" + block.getHash() + "'");

            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next())
            {
                resultData.add(SerializeDeserializeService.parseVeriBlockPublication(resultSet.getBytes(VeriBlockPublicationRepository.veriBlockPublicationColumnName)));
            }
        }
        finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }

        return resultData;
    }
}
