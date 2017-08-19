
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.Currency;
import egl.MonetarySystem;
import egl.Eagle;
import egl.EagleException;
import egl.Transaction;
import egl.TransactionScheduler;
import egl.db.DbIterator;
import egl.util.Convert;
import egl.util.Filter;
import egl.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;

public final class ScheduleCurrencyBuy extends CreateTransaction {

    static final ScheduleCurrencyBuy instance = new ScheduleCurrencyBuy();

    private ScheduleCurrencyBuy() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "currency", "rateNQT", "units", "offerIssuer",
                "transactionJSON", "transactionBytes", "prunableAttachmentJSON");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        String transactionJSON = Convert.emptyToNull(req.getParameter("transactionJSON"));
        String transactionBytes = Convert.emptyToNull(req.getParameter("transactionBytes"));
        String prunableAttachmentJSON = Convert.emptyToNull(req.getParameter("prunableAttachmentJSON"));
        long offerIssuerId = ParameterParser.getAccountId(req, "offerIssuer", true);

        try {
            JSONObject response;
            Transaction transaction;
            if (transactionBytes == null && transactionJSON == null) {
                boolean broadcast = !"false".equalsIgnoreCase(req.getParameter("broadcast"));
                if (broadcast) {
                    return JSONResponses.error("Must use broadcast=false to schedule a future currency buy");
                }
                Currency currency = ParameterParser.getCurrency(req);
                long rateNQT = ParameterParser.getLong(req, "rateNQT", 0, Long.MAX_VALUE, true);
                long units = ParameterParser.getLong(req, "units", 0, Long.MAX_VALUE, true);
                Account account = ParameterParser.getSenderAccount(req);
                String secretPhrase = ParameterParser.getSecretPhrase(req, false);
                Attachment attachment = new Attachment.MonetarySystemExchangeBuy(currency.getId(), rateNQT, units);
                response = (JSONObject)JSONValue.parse(JSON.toString(createTransaction(req, account, attachment)));
                if (secretPhrase == null) {
                    response.put("scheduled", false);
                    return response;
                }
                transaction = Eagle.newTransactionBuilder((JSONObject) response.get("transactionJSON")).build();
            } else {
                response = new JSONObject();
                transaction = ParameterParser.parseTransaction(transactionJSON, transactionBytes, prunableAttachmentJSON).build();
                JSONObject json = JSONData.unconfirmedTransaction(transaction);
                response.put("transactionJSON", json);
                try {
                    response.put("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));
                } catch (EagleException.NotYetEncryptedException ignore) {}
                response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
                response.put("signatureHash", json.get("signatureHash"));
                response.put("transaction", transaction.getStringId());
                response.put("fullHash", transaction.getFullHash());
            }

            Attachment.MonetarySystemExchangeBuy attachment = (Attachment.MonetarySystemExchangeBuy)transaction.getAttachment();
            Filter<Transaction> filter = new ExchangeOfferFilter(offerIssuerId, attachment.getCurrencyId(), attachment.getRateNQT());

            Eagle.getBlockchain().readLock();
            try {
                transaction.validate();
                try (DbIterator<? extends Transaction> unconfirmedTransactions = Eagle.getTransactionProcessor().getAllUnconfirmedTransactions()) {
                    while (unconfirmedTransactions.hasNext()) {
                        if (filter.ok(unconfirmedTransactions.next())) {
                            Eagle.getTransactionProcessor().broadcast(transaction);
                            response.put("broadcasted", true);
                            return response;
                        }
                    }
                }
                TransactionScheduler.schedule(filter, transaction);
                response.put("scheduled", true);
            } finally {
                Eagle.getBlockchain().readUnlock();
            }
            return response;

        } catch (EagleException.InsufficientBalanceException e) {
            return JSONResponses.NOT_ENOUGH_FUNDS;
        }
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }


    private static class ExchangeOfferFilter implements Filter<Transaction> {

        private final long senderId;
        private final long currencyId;
        private final long rateNQT;

        private ExchangeOfferFilter(long senderId, long currencyId, long rateNQT) {
            this.senderId = senderId;
            this.currencyId = currencyId;
            this.rateNQT = rateNQT;
        }

        @Override
        public boolean ok(Transaction transaction) {
            if (transaction.getSenderId() != senderId
                    || transaction.getType() != MonetarySystem.PUBLISH_EXCHANGE_OFFER
                    || transaction.getPhasing() != null) {
                return false;
            }
            Attachment.MonetarySystemPublishExchangeOffer attachment = (Attachment.MonetarySystemPublishExchangeOffer)transaction.getAttachment();
            if (attachment.getCurrencyId() != currencyId || attachment.getSellRateNQT() > rateNQT) {
                return false;
            }
            return true;
        }

    }


}
