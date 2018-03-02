/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.manager;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.consensus.CasperHybridConsensusStrategy;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sync.SyncManager;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.blockchain.EtherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.LOGGED_OUT;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.UNINITIATED;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.VOTING;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.WAITING_FOR_LOGIN;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.WAITING_FOR_LOGOUT;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.WAITING_FOR_VALCODE;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.WAITING_FOR_WITHDRAWABLE;
import static org.ethereum.manager.CasperValidatorService.ValidatorState.WAITING_FOR_WITHDRAWN;

@Component
@Lazy
public class CasperValidatorService {
    private static final Logger logger = LoggerFactory.getLogger("casper.validator");

    @Autowired
    private SyncManager syncManager;

    @Autowired
    private CasperHybridConsensusStrategy strategy;

    private Repository repository;

    private SystemProperties config;

    private Ethereum ethereum;

    private byte[] valContractAddress = null;

    private ECKey coinbase;

    private static final int LOGOUT_COOLDOWN = 60 * 1000;  // In millis

    public static final long DEFAULT_GASLIMIT = 3_141_592;  // Just copied it, don't ask!

    private Map<Long, byte[]> votes = new HashMap<>();

    private BigInteger depositSize;

    private long latestTargetEpoch = -1;
    private long latestSourceEpoch = -1;

    private long lastLogoutBroadcast = -1;


    private byte[] validationContractCode(byte[] address) {
        // FIXME: It's better to have some contract here
        // Originally LLL:
        //    ['seq',
        //        ['return', [0],
        //            ['lll',
        //                ['seq',
        //                    ['calldatacopy', 0, 0, 128],
        //                    ['call', 3000, 1, 0, 0, 128, 0, 32],
        //                    ['mstore', 0, ['eq', ['mload', 0], <int address>]],
        //                    ['return', 0, 32]
        //                ],
        //            [0]]
        //        ]
        //    ]
        String part1 = "61003d5660806000600037602060006080600060006001610bb8f15073";
        String part2 = "6000511460005260206000f35b61000461003d0361000460003961000461003d036000f3";
        return Hex.decode(part1 + Hex.toHexString(address) + part2);
    }

    private byte[] makeVote(long validatorIndex, byte[] targetHash, long targetEpoch, long sourceEpoch, ECKey sender) {
        byte[] sigHash = sha3(RLP.encodeList(validatorIndex, new ByteArrayWrapper(targetHash), targetEpoch, sourceEpoch));
        byte[] vrs = make3IntSignature(sigHash, sender);
        return RLP.encodeList(validatorIndex, new ByteArrayWrapper(targetHash), targetEpoch, sourceEpoch, new ByteArrayWrapper(vrs));
    }

    private byte[] makeLogout(long validatorIndex, long epoch, ECKey sender) {
        byte[] sigHash = sha3(RLP.encodeList(validatorIndex, epoch));
        byte[] vrs = make3IntSignature(sigHash, sender);
        return RLP.encodeList(validatorIndex, epoch, new ByteArrayWrapper(vrs));
    }

    private byte[] make3IntSignature(byte[] data, ECKey signer) {
        ECKey.ECDSASignature signature = signer.sign(data);
        byte[] v, r, s;  // encoding as 32-byte ints
        v = BigIntegers.asUnsignedByteArray(32, BigInteger.valueOf(signature.v));  // FIXME: If we'll have chainId it would fail
        r = BigIntegers.asUnsignedByteArray(32, signature.r);
        s = BigIntegers.asUnsignedByteArray(32, signature.s);
        byte[] vr = ArrayUtils.addAll(v, r);
        byte[] vrs = ArrayUtils.addAll(vr, s);

        return vrs;
    }

    public enum ValidatorState {
        UNINITIATED(1),              // Check if logged in, and if not deploy a valcode contract
        WAITING_FOR_VALCODE(2),      // Wait for valcode contract to be included, then submit deposit
        WAITING_FOR_LOGIN(3),        // Wait for validator to login, then change state to `voting`
        VOTING(4),                   // Vote on each new epoch
        WAITING_FOR_LOGOUT(5),       //
        WAITING_FOR_WITHDRAWABLE(6),
        WAITING_FOR_WITHDRAWN(7),
        LOGGED_OUT(8);

        int i;
        ValidatorState(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    private ValidatorState state = UNINITIATED;

    private Map<ValidatorState, Runnable> handlers = new HashMap<>();

    @Autowired
    public CasperValidatorService(Ethereum ethereum, SystemProperties config) {
        this.ethereum = ethereum;
        this.repository = (org.ethereum.core.Repository) ethereum.getRepository();
        this.config = config;
        this.coinbase = ECKey.fromPrivate(config.getCasperValidatorPrivateKey());
        this.depositSize = EtherUtil.convert(config.getCasperValidatorDeposit(), EtherUtil.Unit.ETHER);

        handlers.put(UNINITIATED, this::checkLoggedIn);
        handlers.put(WAITING_FOR_VALCODE, this::checkValcode);
        handlers.put(WAITING_FOR_LOGIN, this::checkLoggedIn);
        handlers.put(VOTING, this::vote);
        handlers.put(WAITING_FOR_LOGOUT, this::voteThenLogout);
        handlers.put(WAITING_FOR_WITHDRAWABLE, this::checkWithdrawable);
        handlers.put(WAITING_FOR_WITHDRAWN, this::checkWithdrawn);
        handlers.put(LOGGED_OUT, this::checkLoggedIn);
        // FIXME: Actually we should listen only to HEAD changes
        ethereum.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary) {
                newBlockConsumer().accept(blockSummary.getBlock());
            }
        });
    }

    private Consumer<Block> newBlockConsumer() {
        return block -> {
            if(!syncManager.isSyncDone()) return;
            logCasperInfo();

            handlers.get(state).run();
        };
    }

    public void reLogin() {
        if (!state.equals(LOGGED_OUT)) {
            throw new RuntimeException(String.format("Validator is not logged, out, cannot relogin. " +
                    "Current state: %s", state));
        }
        this.depositSize = EtherUtil.convert(config.getCasperValidatorDeposit(), EtherUtil.Unit.ETHER);
    }

    /**
     * Check if logged in, and if not deploy a valcode contract
     */
    private void checkLoggedIn() {
        Long validatorIndex = getValidatorIndex();
        // (1) Check if the validator has ever deposited funds
        if (validatorIndex == 0 && depositSize.compareTo(BigInteger.ZERO) > 0) {
            // The validator hasn't deposited funds but deposit flag is set, so deposit!
            broadcastValcodeTx();
            setState(WAITING_FOR_VALCODE);
            return;
        } else if (validatorIndex == 0) {
            // The validator hasn't deposited funds and we have no intention to, so return!
            return;
        }
        initValContractAddress();
        this.depositSize = BigInteger.ZERO;  // FIXME: Is it correct place for it?

        // (2) Check if the validator is logged in
        long currentEpoch = getCurrentEpoch();
        if (!isLoggedIn(currentEpoch, validatorIndex)) {
            // The validator isn't logged in, so return!
            return;
        }
        if (config.getCasperValidatorShouldLogout()) {
            setState(WAITING_FOR_LOGOUT);
        } else{
            setState(VOTING);
        }
    }

    private void broadcastValcodeTx() {
        Transaction tx = makeTx(null, null, validationContractCode(coinbase.getAddress()), null, null, null, null);

        ethereum.sendTransaction(tx,
                transaction -> {
                    logger.info("Validation code contract creation tx sent, txHash: {}", ByteUtil.toHexString(tx.getHash()));
                    valContractAddress = transaction.getContractAddress();  // FIXME: it's not true until we didn't get tx exec in our state
                },
                throwable -> logger.error("Validation code contract creation tx was not sent", throwable),
                throwable -> logger.warn("Simulation of validation contract tx execution failed", throwable)
        );
    }

    private Transaction makeTx(byte[] receiveAddress, BigInteger value, byte[] data, byte[] gasPrice, byte[] gasLimit,
                               BigInteger nonce, Boolean signed) {
        if (nonce == null) {
            nonce = repository.getNonce(coinbase.getAddress());
        }
        if (gasLimit == null) {
            gasLimit = ByteUtil.longToBytes(DEFAULT_GASLIMIT);
        }
        if (value == null) {
            value = BigInteger.ZERO;
        }
        if (data == null) {
            data = new byte[0];
        }
        if (signed == null) {
            signed = Boolean.TRUE;
        }
        if (gasPrice == null && signed) {
            gasPrice = ByteUtil.bigIntegerToBytes(BigInteger.valueOf(110).multiply(BigInteger.TEN.pow(9)));
        }

        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                gasPrice,
                gasLimit,
                receiveAddress,
                ByteUtil.bigIntegerToBytes(value),
                data);

        if (signed) tx.sign(coinbase);

        return tx;
    }



    private long getValidatorIndex() {
        return constCallCasperForLong("get_validator_indexes", new ByteArrayWrapper(coinbase.getAddress()));
    }

    private void initValContractAddress() {  // Actually it's not used after deposit
        if (valContractAddress != null)
            return;
        byte[] address = (byte[]) strategy.constCallCasper("get_validators__addr", getValidatorIndex())[0];
        if (!Arrays.equals(address, new byte[20])) {
            logger.info("Valcode contract found at {}", address);
            this.valContractAddress = address;
        }
    }

    /**
     * Wait for valcode contract to be included, then submit deposit
     */
    private void checkValcode() {
        if (valContractAddress == null || repository.getCode(valContractAddress) == null ||
                repository.getCode(valContractAddress).length == 0) {
            // Valcode still not deployed! or lost
            return;
        }
        if (repository.getBalance(coinbase.getAddress()).compareTo(depositSize) < 0) {
            logger.info("Cannot login as validator: Not enough ETH! Balance: {}, Deposit: {}",
                    repository.getBalance(coinbase.getAddress()), depositSize);
            return;
        }
        broadcastDepositTx();
        setState(WAITING_FOR_LOGIN);
    }


    private void broadcastDepositTx() {
        // Create deposit transaction
        logger.info("Broadcasting deposit tx on value: {}", depositSize);  // TODO: make me more readable than wei
        Transaction depositTx = makeDepositTx(valContractAddress, coinbase.getAddress(), depositSize);
        this.depositSize = BigInteger.ZERO;
        ethereum.sendTransaction(depositTx,
                tx -> logger.info("Deposit tx successfully submitted to the net, txHash {}", ByteUtil.toHexString(tx.getHash())),
                throwable -> logger.error("Failed to send deposit tx", throwable),
                throwable -> logger.warn("Simulation of deposit tx execution failed", throwable)
        );
        logger.info("Broadcasting deposit tx {}", depositTx);
    }

    private Transaction makeDepositTx(byte[] valContractAddress, byte[] coinbaseAddress, BigInteger deposit) {
        byte[] functionCallBytes = strategy.getCasper().getByName("deposit").encode(
                new ByteArrayWrapper(valContractAddress),
                new ByteArrayWrapper(coinbaseAddress));
        Transaction tx = makeTx(strategy.getCasperAddress(), deposit, functionCallBytes, null, ByteUtil.longToBytes(1_000_000),
                null, true);
        return tx;

    }

    private Transaction makeVoteTx(byte[] voteData) {
        byte[] functionCallBytes = strategy.getCasper().getByName("vote").encode(voteData);
        Transaction tx = makeTx(strategy.getCasperAddress(), null, functionCallBytes, null, ByteUtil.longToBytes(1_000_000),
                BigInteger.ZERO, false);
        return tx;

    }

    private Transaction makeLogoutTx(byte[] logoutData) {
        byte[] functionCallBytes = strategy.getCasper().getByName("logout").encode(new ByteArrayWrapper(logoutData));
        Transaction tx = makeTx(strategy.getCasperAddress(), null, functionCallBytes, null, null,
                null, true);
        return tx;

    }

    private Transaction makeWithdrawTx(long validatorIndex) {
        byte[] functionCallBytes = strategy.getCasper().getByName("withdraw").encode(validatorIndex);
        Transaction tx = makeTx(strategy.getCasperAddress(), null, functionCallBytes, null, null,
                null, true);
        return tx;
    }

    private boolean isLoggedIn(long targetEpoch, long validatorIndex) {
        long startDynasty = constCallCasperForLong("get_validators__start_dynasty", validatorIndex);
        long endDynasty = constCallCasperForLong("get_validators__end_dynasty", validatorIndex);
        long currentDynasty = constCallCasperForLong("get_dynasty_in_epoch", targetEpoch);
        long pastDynasty = currentDynasty - 1;
        boolean inCurrentDynasty = ((startDynasty <= currentDynasty) &&
                (currentDynasty < endDynasty));
        boolean inPrevDynasty = ((startDynasty <= pastDynasty) &&
                (pastDynasty < endDynasty));

        return inCurrentDynasty || inPrevDynasty;
    }

    // TODO: integrate with composite ethereum listener
    private void setState(ValidatorState newState) {
        logger.info("Changing validator state from {} to {}", state, newState);
        this.state = newState;
    }

    private byte[] getEpochBlockHash(long epoch) {
        if (epoch == 0) {
            return Hex.decode("0000000000000000000000000000000000000000000000000000000000000000");
        }
        return strategy.getBlockchain().getBlockByNumber(epoch * config.getCasperEpochLength() - 1).getHash();
    }

    public void voteThenLogout() {
        long epoch = getEpoch();
        long validatorIndex = getValidatorIndex();
        // Verify that we are not already logged out
        if (!isLoggedIn(epoch, validatorIndex)) {
            // If we logged out, start waiting for withdrawal
            logger.info("Validator logged out!");
            setState(WAITING_FOR_WITHDRAWABLE);
            return;
        }
        vote();
        broadcastLogoutTx();
        setState(WAITING_FOR_LOGOUT);
    }

    // FIXME: WHY there are 2 methods for the same thing???
    private long getEpoch() {
        return strategy.getBlockchain().getBestBlock().getNumber() / config.getCasperEpochLength(); // floor division
    }

    private long getCurrentEpoch() {  // FIXME: WHY there are 2 methods for the same thing???
        return constCallCasperForLong("get_current_epoch");
    }

    private boolean vote() {
        long epoch = getEpoch();

        // NO DOUBLE VOTE: Don't vote if we have already
        if (votes.containsKey(epoch)) {
            return false;
        }

        long validatorIndex = getValidatorIndex();

        // Make sure we are logged in
        if (!isLoggedIn(epoch, validatorIndex)) {
            throw new RuntimeException("Cannot vote: Validator not logged in!");
        }

        // Don't start too early
        if (strategy.getBlockchain().getBestBlock().getNumber() % config.getCasperEpochLength() <= config.getCasperEpochLength() / 4) {
            return false;
        }

        byte[] voteData = getRecommendedVoteData(validatorIndex);
        votes.put(epoch, voteData);
        // FIXME: I don't like that epoch could be not the same as in data
        // FIXME: also synchronize required !!!

        // Send the vote!
        Transaction voteTx = makeVoteTx(voteData);
        ethereum.sendTransaction(voteTx,
                tx -> logger.info("Vote sent!, txHash: {}", ByteUtil.toHexString(tx.getHash())),
                throwable -> logger.error("Failed to sent vote", throwable),
                throwable -> logger.warn("Simulation of vote tx exec failed", throwable)
        );
        logger.info("Sending vote tx: {}", voteTx);

        return true;
    }

    private void broadcastLogoutTx() {
        if (lastLogoutBroadcast > (System.currentTimeMillis() - LOGOUT_COOLDOWN)) {
            return;
        }

        this.lastLogoutBroadcast = System.currentTimeMillis();
        long epoch = getEpoch();

        // Generate the message
        byte[] logoutData = makeLogout(getValidatorIndex(), epoch, coinbase);
        Transaction logoutTx = makeLogoutTx(logoutData);
        logger.info("Logout Tx broadcasted: {}", logoutTx);
        ethereum.sendTransaction(logoutTx,
                transaction -> logger.info("Successfully sent logout tx, txHash: {}", ByteUtil.toHexString(transaction.getHash())),
                throwable -> logger.error("Failed to send logout", throwable),
                throwable -> logger.warn("Simulation of logout tx exec failed", throwable)
        );
    }

    private void checkWithdrawable() {
        long validatorIndex = getValidatorIndex();
        if (validatorIndex == 0) {
            logger.info("Validator is already deleted!");
            setState(LOGGED_OUT);
            return;
        }
        long validatorEndDynasty = constCallCasperForLong("get_validators__end_dynasty", validatorIndex);
        long endEpoch = constCallCasperForLong("get_dynasty_start_epoch", validatorEndDynasty + 1);

        // Check Casper to see if we can withdraw
        long curEpoch = getCurrentEpoch();
        long withdrawalDelay = constCallCasperForLong("get_withdrawal_delay");
        if (curEpoch >= (endEpoch + withdrawalDelay)) {
            // Make withdraw tx & broadcast
            Transaction withdrawTx = makeWithdrawTx(validatorIndex);
            ethereum.sendTransaction(withdrawTx,
                    transaction -> logger.info("Successfully sent withdraw tx, txHash {}", ByteUtil.toHexString(transaction.getHash())),
                    throwable -> logger.error("Failed to send withdraw", throwable),
                    throwable -> logger.warn("Simulation of withdraw tx exec failed", throwable)
            );
            // Set the state to waiting for withdrawn
            setState(WAITING_FOR_WITHDRAWN);
        }
    }

    private void checkWithdrawn() {
        // Check that we have been withdrawn--validator index will now be zero
        if (constCallCasperForLong("get_validator_indexes", new ByteArrayWrapper(coinbase.getAddress())) == 0) {
            setState(LOGGED_OUT);
        }
    }

    private void logCasperInfo() {
        long curEpoch = getCurrentEpoch();
        long expectedSourceEpoch = constCallCasperForLong("get_expected_source_epoch");
        BigInteger curDeposits = (BigInteger) strategy.constCallCasper("get_total_curdyn_deposits")[0];
        BigInteger prevDeposits = (BigInteger) strategy.constCallCasper("get_total_prevdyn_deposits")[0];
        BigDecimal curVotes = (BigDecimal) strategy.constCallCasper("get_votes__cur_dyn_votes", curEpoch, expectedSourceEpoch)[0];
        BigDecimal prevVotes = (BigDecimal) strategy.constCallCasper("get_votes__prev_dyn_votes", curEpoch, expectedSourceEpoch)[0];
        BigDecimal scaleFactor = (BigDecimal) strategy.constCallCasper("get_deposit_scale_factor", curEpoch)[0];
        BigDecimal curVotesScaled = curVotes.multiply(scaleFactor);
        BigDecimal prevVotesScaled = prevVotes.multiply(scaleFactor);
        BigDecimal curVotesPct = BigDecimal.ZERO;
        BigDecimal prevVotesPct = BigDecimal.ZERO;
        if (curDeposits.compareTo(BigInteger.ZERO) > 0 ) {
            curVotesPct = curVotesScaled.multiply(BigDecimal.valueOf(100)).divide(new BigDecimal(curDeposits), MathContext.DECIMAL32);
        }
        if (prevDeposits.compareTo(BigInteger.ZERO) > 0 ) {
            prevVotesPct = prevVotesScaled.multiply(BigDecimal.valueOf(100)).divide(new BigDecimal(prevDeposits), MathContext.DECIMAL32);
        }

        long lastFinalizedEpoch = constCallCasperForLong("get_last_finalized_epoch");
        long lastJustifiedEpoch = constCallCasperForLong("get_last_justified_epoch");
        BigDecimal lastNonvoterRescale = (BigDecimal) strategy.constCallCasper("get_last_nonvoter_rescale")[0];
        BigDecimal lastVoterRescale = (BigDecimal) strategy.constCallCasper("get_last_voter_rescale")[0];
        String logStr = String.format(
                "CASPER STATUS: epoch %d, %.3f / %.3f ETH (%.2f %%) voted from current dynasty, " +
                        "%.3f / %.3f ETH (%.2f %%) voted from previous dynasty, last finalized epoch %d justified %d " +
                        "expected source %d. Nonvoter deposits last rescaled %.5fx, voter deposits %.5fx",
                curEpoch,
                curVotesScaled.divide(BigDecimal.TEN.pow(18), MathContext.DECIMAL32),
                new BigDecimal(curDeposits).divide(BigDecimal.TEN.pow(18), MathContext.DECIMAL32),
                curVotesPct,
                prevVotesScaled.divide(BigDecimal.TEN.pow(18), MathContext.DECIMAL32),
                new BigDecimal(prevDeposits).divide(BigDecimal.TEN.pow(18), MathContext.DECIMAL32),
                prevVotesPct,
                lastFinalizedEpoch,
                lastJustifiedEpoch,
                expectedSourceEpoch,
                lastNonvoterRescale,
                lastVoterRescale);
        logger.info(logStr);

        long valIndex = getValidatorIndex();
        BigDecimal myDeposit = (BigDecimal) strategy.constCallCasper("get_validators__deposit", valIndex)[0];
        BigDecimal myDepositScaled = myDeposit.multiply(scaleFactor);
        String myStr = String.format(
                "MY VALIDATOR STATUS: epoch %d, index #%d, deposit: %.3f ETH",
                curEpoch,
                valIndex,
                myDepositScaled.divide(BigDecimal.TEN.pow(18), MathContext.DECIMAL32)
        );
        logger.info(myStr);
    }

    private byte[] getRecommendedVoteData(long validatorIndex) {
        long curEpoch = getCurrentEpoch();
        if (curEpoch == 0) {
            return null;
        }
        // NOTE: Using `epoch_blockhash` because currently calls to `blockhash` within contracts
        // in the ephemeral state are off by one, so we can't use `get_recommended_target_hash()` :(
        //        target_hash = self.epoch_blockhash(current_epoch)
        // ANSWER: Though, I'll try

        byte[] targetHash = (byte[]) strategy.constCallCasper("get_recommended_target_hash")[0];
        long sourceEpoch = constCallCasperForLong("get_recommended_source_epoch");

        if (targetHash == null) {
            return null;
        }

        // Prevent NO_SURROUND slash
        if (curEpoch < latestTargetEpoch || sourceEpoch < latestSourceEpoch) {
            return null;
        }
        this.latestTargetEpoch = curEpoch;
        this.latestSourceEpoch = sourceEpoch;

        return makeVote(validatorIndex, targetHash, curEpoch, sourceEpoch, coinbase);
    }

    private long constCallCasperForLong(String func, Object... funcArgs) {
        Object[] res = strategy.constCallCasper(func, funcArgs);
        return ((BigInteger) res[0]).longValue();
    }

    public void setStrategy(CasperHybridConsensusStrategy strategy) {
        this.strategy = strategy;
    }

    public void setSyncManager(SyncManager syncManager) {
        this.syncManager = syncManager;
    }
}
