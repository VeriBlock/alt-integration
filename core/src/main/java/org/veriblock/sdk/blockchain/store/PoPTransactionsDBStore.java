// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.sqlite.tables.AltPublicationRepository;
import org.veriblock.sdk.sqlite.tables.ContainRepository;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;
import org.veriblock.sdk.sqlite.tables.PoPTransactionsRepository;
import org.veriblock.sdk.sqlite.tables.PoPTransactionsVeriblockPublicationRefRepository;
import org.veriblock.sdk.sqlite.tables.VeriBlockPublicationRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PoPTransactionsDBStore implements PoPTransactionStore {
    private static final Logger log = LoggerFactory.getLogger(AuditorChangesStore.class);

    private Connection connectionResource;

    private PoPTransactionsRepository popTxRepo;
    private ContainRepository containRepo;
    private AltPublicationRepository altPublicationRepo;
    private VeriBlockPublicationRepository veriBlockPublicationRepo;
    private PoPTransactionsVeriblockPublicationRefRepository popTxVeriBlockPublicationRefRepo;

    public PoPTransactionsDBStore(Connection connection) throws SQLException
    {
        this.connectionResource = connection;

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
        String altPublicationHash = altPublicationRepo.save(popTx.altPublication);

        popTxRepo.save(popTx.txHash, endorsedBlock.getHash(), altPublicationHash);
        containRepo.save(popTx.txHash, containingBlock.getHash());

        for(VeriBlockPublication publication: popTx.veriBlockPublications)
        {
            String veriBlockPublicationHash = veriBlockPublicationRepo.save(publication);
            popTxVeriBlockPublicationRefRepo.save(popTx.txHash, veriBlockPublicationHash);
        }
    }

    public List<AltPublication> getAltPublicationsEndorse(AltChainBlock endorsedBlock, List<AltChainBlock> containBlocks) throws SQLException
    {
        List<AltPublication> resultData = new ArrayList<AltPublication>();

        PreparedStatement stmt = null;
        try{
            StringBuilder sql = new StringBuilder("SELECT DISTINCT " +  AltPublicationRepository.tableName + "." + AltPublicationRepository.altPublicationDataColumnName +
                    " FROM " + PoPTransactionsRepository.tableName + " LEFT JOIN " + AltPublicationRepository.tableName +
                    " ON " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.altPublicationHashColumnName +
                    " = " + AltPublicationRepository.tableName + "." + AltPublicationRepository.altPublicationHash +
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
                resultData.add(SerializeDeserializeService.parseAltPublication(resultSet.getBytes(AltPublicationRepository.altPublicationDataColumnName)));
            }
        }
        finally {
            if(stmt != null) stmt.close();
        }

        return resultData;
    }

    public List<AltPublication> getAltPublicationsFromBlock(AltChainBlock block) throws SQLException
    {
        List<AltPublication> resultData = new ArrayList<AltPublication>();

        PreparedStatement stmt = null;
        try{
            stmt = connectionResource.prepareStatement( " SELECT DISTINCT " + AltPublicationRepository.tableName + "." + AltPublicationRepository.altPublicationDataColumnName +
                    " FROM " + PoPTransactionsRepository.tableName + " LEFT JOIN " + AltPublicationRepository.tableName +
                    " ON " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.altPublicationHashColumnName +
                    " = " + AltPublicationRepository.tableName + "." + AltPublicationRepository.altPublicationHash +
                    " LEFT JOIN " + ContainRepository.tableName +
                    " ON " + PoPTransactionsRepository.tableName + "." + PoPTransactionsRepository.txHashColumnName +
                    " = " + ContainRepository.tableName + "." + ContainRepository.txHashColumnName +
                    " WHERE " + ContainRepository.tableName + "." + ContainRepository.blockHashColumnName + " = '" + block.getHash() +"'");

            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next())
            {
                resultData.add(SerializeDeserializeService.parseAltPublication(resultSet.getBytes(AltPublicationRepository.altPublicationDataColumnName)));
            }
        }
        finally {
            if(stmt != null) stmt.close();
        }

        return resultData;
    }

    public List<VeriBlockPublication> getVeriBlockPublicationsFromBlock(AltChainBlock block) throws SQLException
    {
        List<VeriBlockPublication> resultData = new ArrayList<VeriBlockPublication>();

        PreparedStatement stmt = null;
        try{
            stmt = connectionResource.prepareStatement( " SELECT DISTINCT " + VeriBlockPublicationRepository.tableName + "." + VeriBlockPublicationRepository.veriBlockPublicationDataColumnName +
                    " FROM " + VeriBlockPublicationRepository.tableName + " LEFT JOIN " + PoPTransactionsVeriblockPublicationRefRepository.tableName +
                    " ON " + PoPTransactionsVeriblockPublicationRefRepository.tableName + "." + PoPTransactionsVeriblockPublicationRefRepository.veriBlockPublicationHashColumnName +
                    " = " + VeriBlockPublicationRepository.tableName + "." + VeriBlockPublicationRepository.veriBlockPublicationHashColumnName +
                    " LEFT JOIN " + ContainRepository.tableName +
                    " ON " + PoPTransactionsVeriblockPublicationRefRepository.tableName + "." + PoPTransactionsVeriblockPublicationRefRepository.txHashColumnName +
                    " = " + ContainRepository.tableName + "." + ContainRepository.txHashColumnName +
                    " WHERE " + ContainRepository.tableName + "." + ContainRepository.blockHashColumnName + " = '" + block.getHash() + "'");

            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next())
            {
                resultData.add(SerializeDeserializeService.parseVeriBlockPublication(resultSet.getBytes(VeriBlockPublicationRepository.veriBlockPublicationDataColumnName)));
            }
        }
        finally {
            if(stmt != null) stmt.close();
        }

        return resultData;
    }
}
