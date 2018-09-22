package org.ethereum.kafka.models;

import java.util.List;
import org.ethereum.core.TransactionInfo;

public class TransactionInfoList {
    private final List<TransactionInfo> infos;

    public TransactionInfoList(List<TransactionInfo> infos) {
      this.infos = infos;
    }

    public List<TransactionInfo> getInfos() {
      return infos;
    }
}