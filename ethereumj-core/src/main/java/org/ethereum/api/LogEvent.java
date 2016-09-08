package org.ethereum.api;

import org.ethereum.core.TransactionInfo;
import org.ethereum.api.type.*;

import java.util.List;

/**
 * Created by Anton Nashatyrev on 08.09.2016.
 */
public interface LogEvent {

    BlockId getBlock();

    TransactionInfo getTransaction();

    int getLogIndex();

    Address getContractAddress();

    List<LogTopic> getTopics();

    ByteArray getData();

}
