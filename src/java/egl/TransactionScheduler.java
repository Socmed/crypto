
package egl;

import egl.util.Filter;
import egl.util.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionScheduler {

    private static final Map<Transaction, TransactionScheduler> transactionSchedulers = new ConcurrentHashMap<>();

    public static void schedule(Filter<Transaction> filter, Transaction transaction) {
        if (transactionSchedulers.size() >= 100) {
            throw new RuntimeException("Cannot schedule more than 100 transactions! Please restart your node if you want to clear existing scheduled transactions.");
        }
        transactionSchedulers.put(transaction, new TransactionScheduler(filter, transaction));
    }

    public static List<Transaction> getScheduledTransactions(long accountId) {
        ArrayList<Transaction> list = new ArrayList<>();
        for (Transaction transaction : transactionSchedulers.keySet()) {
            if (transaction.getSenderId() == accountId) {
                list.add(transaction);
            }
        }
        return list;
    }

    static {
        TransactionProcessorImpl.getInstance().addListener(transactions -> {
            Iterator<Map.Entry<Transaction, TransactionScheduler>> iterator = transactionSchedulers.entrySet().iterator();
            while (iterator.hasNext()) {
                TransactionScheduler transactionScheduler = iterator.next().getValue();
                for (Transaction transaction : transactions) {
                    if (transactionScheduler.processEvent(transaction)) {
                        iterator.remove();
                        Logger.logInfoMessage("Removed " + transaction.getStringId() + " from transaction scheduler");
                        break;
                    }
                }
            }
        }, TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
    }

    static void init() {}

    private final Transaction transaction;
    private final Filter<Transaction> filter;

    private TransactionScheduler(Filter<Transaction> filter, Transaction transaction) {
        this.transaction = transaction;
        this.filter = filter;
    }

    private boolean processEvent(Transaction unconfirmedTransaction) {
        if (transaction.getExpiration() < Eagle.getEpochTime()) {
            Logger.logInfoMessage("Expired transaction in transaction scheduler " + transaction.getSenderId());
            return true;
        }
        if (!filter.ok(unconfirmedTransaction)) {
            return false;
        }
        try {
            TransactionProcessorImpl.getInstance().broadcast(transaction);
            return true;
        } catch (EagleException.ValidationException e) {
            Logger.logInfoMessage("Failed to broadcast: " + e.getMessage());
            return true;
        }
    }

}
