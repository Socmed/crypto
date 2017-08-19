
package egl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;

public class GeneratorTest extends BlockchainTest {

    /**
     * Simulate the forging process calculations
     */
    @Ignore
    @Test
    public void forge() {
        byte[] publicKey = ALICE.getPublicKey();
        BlockImpl lastBlock = blockchain.getLastBlock();
        BigInteger hit = Generator.getHit(publicKey, lastBlock);
        Account account = Account.getAccount(publicKey);
        BigInteger effectiveBalance = BigInteger.valueOf(account == null || account.getEffectiveBalanceEGL() <= 0 ? 0 : account.getEffectiveBalanceEGL());
        long hitTime = Generator.getHitTime(effectiveBalance, hit, lastBlock);
        long deadline = hitTime - lastBlock.getTimestamp();
        Generator generator = Generator.startForging(ALICE.getSecretPhrase());
        int i=1;
        try {
            while (i<deadline) {
                Assert.assertFalse(generator.forge(lastBlock, lastBlock.getTimestamp() + i));
                i += 100;
            }
            Assert.assertEquals(true, generator.forge(lastBlock, (int)hitTime + 1));
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            e.printStackTrace();
        }

        // Now the block is broadcast to all peers
        // This is what the peer which receives the block does
        lastBlock = blockchain.getLastBlock();
        Assert.assertEquals(hitTime + 1, lastBlock.getTimestamp());
        try {
            Assert.assertTrue(lastBlock.verifyGenerationSignature());
        } catch (BlockchainProcessor.BlockOutOfOrderException e) {
            e.printStackTrace();
        }
    }

}
