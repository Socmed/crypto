
package egl;

import egl.crypto.HashFunction;

import java.util.EnumSet;
import java.util.Set;

/**
 * Define and validate currency capabilities
 */
public enum CurrencyType {

    /**
     * Can be exchanged from/to EGL<br>
     */
    EXCHANGEABLE(0x01) {

        @Override
        void validate(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.NotValidException {
        }

        @Override
        void validateMissing(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.NotValidException {
            if (transaction.getType() == MonetarySystem.CURRENCY_ISSUANCE) {
                if (!validators.contains(CLAIMABLE)) {
                    throw new EagleException.NotValidException("Currency is not exchangeable and not claimable");
                }
            }
            if (transaction.getType() instanceof MonetarySystem.MonetarySystemExchange || transaction.getType() == MonetarySystem.PUBLISH_EXCHANGE_OFFER) {
                throw new EagleException.NotValidException("Currency is not exchangeable");
            }
        }
    },
    /**
     * Transfers are only allowed from/to issuer account<br>
     * Only issuer account can publish exchange offer<br>
     */
    CONTROLLABLE(0x02) {

        @Override
        void validate(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.NotValidException {
            if (transaction.getType() == MonetarySystem.CURRENCY_TRANSFER) {
                if (currency == null ||  (currency.getAccountId() != transaction.getSenderId() && currency.getAccountId() != transaction.getRecipientId())) {
                    throw new EagleException.NotValidException("Controllable currency can only be transferred to/from issuer account");
                }
            }
            if (transaction.getType() == MonetarySystem.PUBLISH_EXCHANGE_OFFER) {
                if (currency == null || currency.getAccountId() != transaction.getSenderId()) {
                    throw new EagleException.NotValidException("Only currency issuer can publish an exchange offer for controllable currency");
                }
            }
        }

        @Override
        void validateMissing(Currency currency, Transaction transaction, Set<CurrencyType> validators) {}

    },
    /**
     * Can be reserved before the currency is active, reserve is distributed to founders once the currency becomes active<br>
     */
    RESERVABLE(0x04) {

        @Override
        void validate(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.ValidationException {
            if (transaction.getType() == MonetarySystem.CURRENCY_ISSUANCE) {
                Attachment.MonetarySystemCurrencyIssuance attachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
                int issuanceHeight = attachment.getIssuanceHeight();
                int finishHeight = attachment.getFinishValidationHeight(transaction);
                if  (issuanceHeight <= finishHeight) {
                    throw new EagleException.NotCurrentlyValidException(
                        String.format("Reservable currency activation height %d not higher than transaction apply height %d",
                                issuanceHeight, finishHeight));
                }
                if (attachment.getMinReservePerUnitNQT() <= 0) {
                    throw new EagleException.NotValidException("Minimum reserve per unit must be > 0");
                }
                if (Math.multiplyExact(attachment.getMinReservePerUnitNQT(), attachment.getReserveSupply()) > Constants.MAX_BALANCE_NQT) {
                    throw new EagleException.NotValidException("Minimum reserve per unit is too large");
                }
                if (attachment.getReserveSupply() <= attachment.getInitialSupply()) {
                    throw new EagleException.NotValidException("Reserve supply must exceed initial supply");
                }
                if (!validators.contains(MINTABLE) && attachment.getReserveSupply() < attachment.getMaxSupply()) {
                    throw new EagleException.NotValidException("Max supply must not exceed reserve supply for reservable and non-mintable currency");
                }
            }
            if (transaction.getType() == MonetarySystem.RESERVE_INCREASE) {
                Attachment.MonetarySystemReserveIncrease attachment = (Attachment.MonetarySystemReserveIncrease) transaction.getAttachment();
                if (currency != null && currency.getIssuanceHeight() <= attachment.getFinishValidationHeight(transaction)) {
                    throw new EagleException.NotCurrentlyValidException("Cannot increase reserve for active currency");
                }
            }
        }

        @Override
        void validateMissing(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.NotValidException {
            if (transaction.getType() == MonetarySystem.RESERVE_INCREASE) {
                throw new EagleException.NotValidException("Cannot increase reserve since currency is not reservable");
            }
            if (transaction.getType() == MonetarySystem.CURRENCY_ISSUANCE) {
                Attachment.MonetarySystemCurrencyIssuance attachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
                if (attachment.getIssuanceHeight() != 0) {
                    throw new EagleException.NotValidException("Issuance height for non-reservable currency must be 0");
                }
                if (attachment.getMinReservePerUnitNQT() > 0) {
                    throw new EagleException.NotValidException("Minimum reserve per unit for non-reservable currency must be 0 ");
                }
                if (attachment.getReserveSupply() > 0) {
                    throw new EagleException.NotValidException("Reserve supply for non-reservable currency must be 0");
                }
                if (!validators.contains(MINTABLE) && attachment.getInitialSupply() < attachment.getMaxSupply()) {
                    throw new EagleException.NotValidException("Initial supply for non-reservable and non-mintable currency must be equal to max supply");
                }
            }
        }
    },
    /**
     * Is {@link #RESERVABLE} and can be claimed after currency is active<br>
     * Cannot be {@link #EXCHANGEABLE}
     */
    CLAIMABLE(0x08) {

        @Override
        void validate(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.ValidationException {
            if (transaction.getType() == MonetarySystem.CURRENCY_ISSUANCE) {
                Attachment.MonetarySystemCurrencyIssuance attachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
                if (!validators.contains(RESERVABLE)) {
                    throw new EagleException.NotValidException("Claimable currency must be reservable");
                }
                if (validators.contains(MINTABLE)) {
                    throw new EagleException.NotValidException("Claimable currency cannot be mintable");
                }
                if (attachment.getInitialSupply() > 0) {
                    throw new EagleException.NotValidException("Claimable currency must have initial supply 0");
                }
            }
            if (transaction.getType() == MonetarySystem.RESERVE_CLAIM) {
                if (currency == null || !currency.isActive()) {
                    throw new EagleException.NotCurrentlyValidException("Cannot claim reserve since currency is not yet active");
                }
            }
        }

        @Override
        void validateMissing(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.NotValidException {
            if (transaction.getType() == MonetarySystem.RESERVE_CLAIM) {
                throw new EagleException.NotValidException("Cannot claim reserve since currency is not claimable");
            }
        }
    },
    /**
     * Can be minted using proof of work algorithm<br>
     */
    MINTABLE(0x10) {
        @Override
        void validate(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.NotValidException {
            if (transaction.getType() == MonetarySystem.CURRENCY_ISSUANCE) {
                Attachment.MonetarySystemCurrencyIssuance issuanceAttachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
                try {
                    HashFunction hashFunction = HashFunction.getHashFunction(issuanceAttachment.getAlgorithm());
                    if (!CurrencyMinting.acceptedHashFunctions.contains(hashFunction)) {
                        throw new EagleException.NotValidException("Invalid minting algorithm " + hashFunction);
                    }
                } catch (IllegalArgumentException e) {
                    throw new EagleException.NotValidException("Illegal algorithm code specified" , e);
                }
                if (issuanceAttachment.getMinDifficulty() < 1 || issuanceAttachment.getMaxDifficulty() > 255 ||
                        issuanceAttachment.getMaxDifficulty() < issuanceAttachment.getMinDifficulty()) {
                    throw new EagleException.NotValidException(
                            String.format("Invalid minting difficulties min %d max %d, difficulty must be between 1 and 255, max larger than min",
                                    issuanceAttachment.getMinDifficulty(), issuanceAttachment.getMaxDifficulty()));
                }
                if (issuanceAttachment.getMaxSupply() <= issuanceAttachment.getReserveSupply()) {
                    throw new EagleException.NotValidException("Max supply for mintable currency must exceed reserve supply");
                }
            }
        }

        @Override
        void validateMissing(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.NotValidException {
            if (transaction.getType() == MonetarySystem.CURRENCY_ISSUANCE) {
                Attachment.MonetarySystemCurrencyIssuance issuanceAttachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
                if (issuanceAttachment.getMinDifficulty() != 0 ||
                        issuanceAttachment.getMaxDifficulty() != 0 ||
                        issuanceAttachment.getAlgorithm() != 0) {
                    throw new EagleException.NotValidException("Non mintable currency should not specify algorithm or difficulty");
                }
            }
            if (transaction.getType() == MonetarySystem.CURRENCY_MINTING) {
                throw new EagleException.NotValidException("Currency is not mintable");
            }
        }

    },
    /**
     * Several accounts can shuffle their currency units and then distributed to recipients<br>
     */
    NON_SHUFFLEABLE(0x20) {
        @Override
        void validate(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.ValidationException {
            if (transaction.getType() == ShufflingTransaction.SHUFFLING_CREATION) {
                throw new EagleException.NotValidException("Shuffling is not allowed for this currency");
            }
        }

        @Override
        void validateMissing(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.ValidationException {
        }
    };

    private final int code;

    CurrencyType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    abstract void validate(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.ValidationException;

    abstract void validateMissing(Currency currency, Transaction transaction, Set<CurrencyType> validators) throws EagleException.ValidationException;

    public static CurrencyType get(int code) {
        for (CurrencyType currencyType : values()) {
            if (currencyType.getCode() == code) {
                return currencyType;
            }
        }
        return null;
    }

    static void validate(Currency currency, Transaction transaction) throws EagleException.ValidationException {
        if (currency == null) {
            throw new EagleException.NotCurrentlyValidException("Unknown currency: " + transaction.getAttachment().getJSONObject());
        }
        validate(currency, currency.getType(), transaction);
    }

    static void validate(int type, Transaction transaction) throws EagleException.ValidationException {
        validate(null, type, transaction);
    }

    private static void validate(Currency currency, int type, Transaction transaction) throws EagleException.ValidationException {
        if (transaction.getAmountNQT() != 0) {
            throw new EagleException.NotValidException("Currency transaction EGL amount must be 0");
        }

        final EnumSet<CurrencyType> validators = EnumSet.noneOf(CurrencyType.class);
        for (CurrencyType validator : CurrencyType.values()) {
            if ((validator.getCode() & type) != 0) {
                validators.add(validator);
            }
        }
        if (validators.isEmpty()) {
            throw new EagleException.NotValidException("Currency type not specified");
        }
        for (CurrencyType validator : CurrencyType.values()) {
            if ((validator.getCode() & type) != 0) {
                validator.validate(currency, transaction, validators);
            } else {
                validator.validateMissing(currency, transaction, validators);
            }
        }
    }

    static void validateCurrencyNaming(long issuerAccountId, Attachment.MonetarySystemCurrencyIssuance attachment) throws EagleException.ValidationException {
        String name = attachment.getName();
        String code = attachment.getCode();
        String description = attachment.getDescription();
        if (name.length() < Constants.MIN_CURRENCY_NAME_LENGTH || name.length() > Constants.MAX_CURRENCY_NAME_LENGTH
                || name.length() < code.length()
                || code.length() < Constants.MIN_CURRENCY_CODE_LENGTH || code.length() > Constants.MAX_CURRENCY_CODE_LENGTH
                || description.length() > Constants.MAX_CURRENCY_DESCRIPTION_LENGTH) {
            throw new EagleException.NotValidException(String.format("Invalid currency name %s code %s or description %s", name, code, description));
        }
        String normalizedName = name.toLowerCase();
        for (int i = 0; i < normalizedName.length(); i++) {
            if (Constants.ALPHABET.indexOf(normalizedName.charAt(i)) < 0) {
                throw new EagleException.NotValidException("Invalid currency name: " + normalizedName);
            }
        }
        for (int i = 0; i < code.length(); i++) {
            if (Constants.ALLOWED_CURRENCY_CODE_LETTERS.indexOf(code.charAt(i)) < 0) {
                throw new EagleException.NotValidException("Invalid currency code: " + code + " code must be all upper case");
            }
        }
        if (code.contains("EGL") || code.contains("NEXT") || "egl".equals(normalizedName) || "next".equals(normalizedName)) {
            throw new EagleException.NotValidException("Currency name already used");
        }
        Currency currency;
        if ((currency = Currency.getCurrencyByName(normalizedName)) != null && ! currency.canBeDeletedBy(issuerAccountId)) {
            throw new EagleException.NotCurrentlyValidException("Currency name already used: " + normalizedName);
        }
        if ((currency = Currency.getCurrencyByCode(name)) != null && ! currency.canBeDeletedBy(issuerAccountId)) {
            throw new EagleException.NotCurrentlyValidException("Currency name already used as code: " + normalizedName);
        }
        if ((currency = Currency.getCurrencyByCode(code)) != null && ! currency.canBeDeletedBy(issuerAccountId)) {
            throw new EagleException.NotCurrentlyValidException("Currency code already used: " + code);
        }
        if ((currency = Currency.getCurrencyByName(code)) != null && ! currency.canBeDeletedBy(issuerAccountId)) {
            throw new EagleException.NotCurrentlyValidException("Currency code already used as name: " + code);
        }
    }

}
