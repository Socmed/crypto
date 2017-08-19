
package egl.addons;

import egl.Block;
import egl.BlockchainProcessor;
import egl.Eagle;
import egl.util.Listener;
import egl.util.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public final class DownloadTimer implements AddOn {

    private PrintWriter writer = null;

    @Override
    public void init() {

        try {

            writer = new PrintWriter((new BufferedWriter(new OutputStreamWriter(new FileOutputStream("downloadtime.csv")))), true);
            writer.println("height,time,dtime,bps,transations,dtransactions,tps");
            Eagle.getBlockchainProcessor().addListener(new Listener<Block>() {

                final int interval = 10000;
                final long startTime = System.currentTimeMillis();
                long previousTime = 0;
                long transactions = 0;
                long dtransactions = 0;

                @Override
                public void notify(Block block) {
                    int n = block.getTransactions().size();
                    transactions += n;
                    dtransactions += n;
                    int height = block.getHeight();
                    if (height % interval == 0) {
                        long time = System.currentTimeMillis() - startTime;
                        writer.print(height);
                        writer.print(',');
                        writer.print(time/1000);
                        writer.print(',');
                        long dtime = (time - previousTime)/1000;
                        writer.print(dtime);
                        writer.print(',');
                        writer.print(interval/dtime);
                        writer.print(',');
                        writer.print(transactions);
                        writer.print(',');
                        writer.print(dtransactions);
                        writer.print(',');
                        long tps = dtransactions/dtime;
                        writer.println(tps);
                        previousTime = time;
                        dtransactions = 0;
                    }
                }

            }, BlockchainProcessor.Event.BLOCK_PUSHED);

        } catch (IOException e) {
            Logger.logErrorMessage(e.getMessage(), e);
        }

    }

    @Override
    public void shutdown() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

}
