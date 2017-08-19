
package egl;

import egl.crypto.Crypto;
import egl.util.Listener;
import egl.util.Logger;
import org.junit.Assert;

import java.util.Properties;

public abstract class AbstractBlockchainTest {

    protected static BlockchainProcessorImpl blockchainProcessor;
    protected static BlockchainImpl blockchain;
    private static final Object doneLock = new Object();
    private static boolean done = false;

    protected static Properties newTestProperties() {
        Properties testProperties = new Properties();
        testProperties.setProperty("egl.shareMyAddress", "false");
        testProperties.setProperty("egl.savePeers", "false");
        //testProperties.setProperty("egl.enableAPIServer", "false");
        //testProperties.setProperty("egl.enableUIServer", "false");
        testProperties.setProperty("egl.disableGenerateBlocksThread", "true");
        //testProperties.setProperty("egl.disableProcessTransactionsThread", "true");
        //testProperties.setProperty("egl.disableRemoveUnconfirmedTransactionsThread", "true");
        //testProperties.setProperty("egl.disableRebroadcastTransactionsThread", "true");
        //testProperties.setProperty("egl.disablePeerUnBlacklistingThread", "true");
        //testProperties.setProperty("egl.getMorePeers", "false");
        testProperties.setProperty("egl.testUnconfirmedTransactions", "true");
        testProperties.setProperty("egl.debugTraceAccounts", "");
        testProperties.setProperty("egl.debugLogUnconfirmed", "false");
        testProperties.setProperty("egl.debugTraceQuote", "\"");
        //testProperties.setProperty("egl.numberOfForkConfirmations", "0");
        return testProperties;
    }

    protected static void init(Properties testProperties) {
        Eagle.init(testProperties);
        blockchain = BlockchainImpl.getInstance();
        blockchainProcessor = BlockchainProcessorImpl.getInstance();
        blockchainProcessor.setGetMoreBlocks(false);
        TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
        Listener<Block> countingListener = block -> {
            if (block.getHeight() % 1000 == 0) {
                Logger.logMessage("downloaded block " + block.getHeight());
            }
        };
        blockchainProcessor.addListener(countingListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected static void shutdown() {
        TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
    }

    protected static void downloadTo(final int endHeight) {
        if (blockchain.getHeight() == endHeight) {
            return;
        }
        Assert.assertTrue(blockchain.getHeight() < endHeight);
        Listener<Block> stopListener = block -> {
            if (blockchain.getHeight() == endHeight) {
                synchronized (doneLock) {
                    done = true;
                    blockchainProcessor.setGetMoreBlocks(false);
                    doneLock.notifyAll();
                    throw new EagleException.StopException("Reached height " + endHeight);
                }
            }
        };
        blockchainProcessor.addListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
        synchronized (doneLock) {
            done = false;
            Logger.logMessage("Starting download from height " + blockchain.getHeight());
            blockchainProcessor.setGetMoreBlocks(true);
            while (! done) {
                try {
                    doneLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertEquals(endHeight, blockchain.getHeight());
        blockchainProcessor.removeListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected static void forgeTo(final int endHeight, final String secretPhrase) {
        if (blockchain.getHeight() == endHeight) {
            return;
        }
        Assert.assertTrue(blockchain.getHeight() < endHeight);
        Listener<Block> stopListener = block -> {
            if (blockchain.getHeight() == endHeight) {
                synchronized (doneLock) {
                    done = true;
                    Generator.stopForging(secretPhrase);
                    doneLock.notifyAll();
                }
            }
        };
        blockchainProcessor.addListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
        synchronized (doneLock) {
            done = false;
            Logger.logMessage("Starting forging from height " + blockchain.getHeight());
            Generator.startForging(secretPhrase);
            while (! done) {
                try {
                    doneLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertTrue(blockchain.getHeight() >= endHeight);
        Assert.assertArrayEquals(Crypto.getPublicKey(secretPhrase), blockchain.getLastBlock().getGeneratorPublicKey());
        blockchainProcessor.removeListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected int getHeight() {
        return blockchain.getHeight();
    }
}
