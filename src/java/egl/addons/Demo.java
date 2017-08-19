
package egl.addons;

import egl.Account;
import egl.BlockchainProcessor;
import egl.Eagle;
import egl.util.Convert;
import egl.util.Logger;

public final class Demo implements AddOn {

    @Override
    public void init() {
        Eagle.getBlockchainProcessor().addListener(block -> Logger.logInfoMessage("Block " + block.getStringId()
                + " has been forged by account " + Convert.rsAccount(block.getGeneratorId()) + " having effective balance of "
                + Account.getAccount(block.getGeneratorId()).getEffectiveBalanceEGL()),
                BlockchainProcessor.Event.BEFORE_BLOCK_APPLY);
    }

    @Override
    public void shutdown() {
        Logger.logInfoMessage("Goodbye!");
    }

}
