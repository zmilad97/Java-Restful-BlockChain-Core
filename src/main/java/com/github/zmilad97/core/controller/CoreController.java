package com.github.zmilad97.core.controller;


import com.github.zmilad97.core.module.Block;
import com.github.zmilad97.core.module.transaction.Transaction;
import com.github.zmilad97.core.module.transaction.TransactionInput;
import com.github.zmilad97.core.module.transaction.TransactionOutput;
import com.github.zmilad97.core.module.Wallet;
import com.github.zmilad97.core.service.CoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.*;

@RestController
public class CoreController {
    private static final Logger LOG = LoggerFactory.getLogger(CoreController.class);
    private final CoreService coreService;

    @Autowired
    public CoreController(CoreService coreService) {
        this.coreService = coreService;

    }

    @GetMapping("/connectionTest")
    public void testConnection() {

    }

    @RequestMapping(value = "/validMine", method = RequestMethod.POST)
    public void validMine(@RequestBody Block block) {
//        Block block =new Block();
        LOG.info("validating : {}", block);
        coreService.addBlock(block);
    }


    @RequestMapping(value = "/transaction/new", method = RequestMethod.POST)
    public void newTransaction(@RequestBody Transaction transaction) {
        transaction.setTransactionId(coreService.getTransactionId());
        coreService.getCurrentTransaction().add(transaction);
    }

/*
    @RequestMapping(value = "/wallet/status", method = RequestMethod.POST)
    public Wallet walletStatus(@RequestBody String pubKey) {
        return null;
    }
*/

    @RequestMapping(value = "/UTXOs", method = RequestMethod.POST)
    public List<Transaction> UTXOs(@RequestBody String signature) {
        LOG.info(signature);
        return coreService.findUTXOs(signature);
    }

    @RequestMapping(value = "/block")
    public ResponseEntity<Block> sendBlock() {
        LOG.debug("sending Block");

        if (coreService.isCurrentTransactionEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Block block = new Block();
        block.setDate(new java.util.Date().toString());
        block.setIndex(coreService.getChain().size());
        block.setPreviousHash(coreService.getChain().get(coreService.getChain().size() - 1).getPreviousHash());
        block.setTransactions(coreService.getCurrentTransaction());
        block.setDifficultyLevel(coreService.getDifficultyLevel());
        block.setReward(coreService.getReward());

        return ResponseEntity.ok(block);
    }

    @RequestMapping(value = "/chain")
    public List<Block> chain() {
        return coreService.getChain();
    }

    @RequestMapping(value = "/wallet/add", method = RequestMethod.POST)
    public void addWallet(@RequestBody String wallet) {
        System.out.println(wallet);
        coreService.addWalletToWalletList(new Wallet(wallet, 0));

    }


    @RequestMapping(value = "/node/register", method = RequestMethod.POST)
    public void registerNode(URL url) {
        coreService.addNode(url.getAuthority());
    }

    @RequestMapping(value = "/resolve", method = RequestMethod.GET)
    public List<Block> resolve() {
        return coreService.resolveConflict();
    }


    @RequestMapping(value = "/test/block", method = RequestMethod.GET)
    public void addTestBlock() {
        String signature = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEF25QbMKZV5wJ/tw9BjBvx137bIQwbJR76bYkwAQeKbn9xRPPaMNpu0hWRlZt8MUxvGvn/ln5PxPHB+cmbmacZw==";
        Transaction transaction = new Transaction();
        transaction.setTransactionHash("test");
        TransactionInput transactionInput = new TransactionInput();
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionInput.setIndexReferenced(12);
        transactionInput.addPreviousTransactionHash(0, "test");
        transactionInput.setPubKey("null");
        transactionOutput.setAmount(50);
        transactionOutput.setSignature(signature);
        transaction.setTransactionId("80");
        transaction.setTransactionInput(transactionInput);
        transaction.setTransactionOutput(transactionOutput);
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);
        LOG.debug(String.valueOf(transaction));
        Block block = new Block(10, String.valueOf(new java.util.Date()), transactionList);
        coreService.getChain().add(block);


    }


    @RequestMapping(value = "/test/transaction", method = RequestMethod.GET)
    public void addTestTransaction() {
        Transaction transaction = new Transaction();
        TransactionInput transactionInput = new TransactionInput();
        TransactionOutput transactionOutput = new TransactionOutput();

        transactionInput.addPreviousTransactionHash(0, "hash 0 test");
        transactionInput.setIndexReferenced(20);
        transactionInput.setPubKey("pubkey test");

        transactionOutput.setSignature("signature test");
        transactionOutput.setAmount(200);

        transaction.setTransactionId("test");
        transaction.setTransactionOutput(transactionOutput);
        transaction.setTransactionInput(transactionInput);
        transaction.setTransactionHash("tst 404");
        coreService.addTransaction(transaction);


    }


}