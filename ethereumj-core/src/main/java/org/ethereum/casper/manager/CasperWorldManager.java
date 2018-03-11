package org.ethereum.casper.manager;

import org.ethereum.casper.core.CasperFacade;
import org.ethereum.casper.service.CasperValidatorService;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.ethereum.casper.core.genesis.CasperStateInit;
import org.ethereum.core.genesis.StateInit;
import org.ethereum.manager.WorldManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class CasperWorldManager extends WorldManager {

    @Autowired
    private CasperValidatorService casperValidatorService;

    @Autowired
    private CasperFacade casper;

    private CasperStateInit stateInit;

    public CasperWorldManager(SystemProperties config, Repository repository, Blockchain blockchain) {
        super(config, repository, blockchain);
    }

    @PostConstruct
    @Override
    protected void init() {
        super.init();
        casper.setEthereum(ethereum);
        casperValidatorService.init();
    }

    @Override
    protected StateInit createStateInit(Genesis genesis) {
        if (stateInit == null) {
            this.stateInit = new CasperStateInit(genesis, (Repository) getRepository(), blockchain, config);
            // FIXME: Looks like not a good side job
            casper.setInitTxs(stateInit.makeInitTxes().getValue());
        }
        return stateInit;
    }
}
