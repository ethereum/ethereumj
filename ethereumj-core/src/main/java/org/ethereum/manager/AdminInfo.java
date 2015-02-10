package org.ethereum.manager;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 11.12.2014
 */
@Component
public class AdminInfo {


    private long startupTimeStamp;
    private boolean consensus = true;
    private List<Long> blockExecTime = new LinkedList<>();


    @PostConstruct
    public void init() {
        startupTimeStamp = System.currentTimeMillis();
    }

    public long getStartupTimeStamp() {
        return startupTimeStamp;
    }

    public boolean isConsensus() {
        return consensus;
    }

    public void lostConsensus() {
        consensus = false;
    }

    public void addBlockExecTime(long time){
        blockExecTime.add(time);
    }

    public Long getExecAvg(){

        if (blockExecTime.size() == 0) return 0L;

        long sum = 0;
        for (int i = 0; i < blockExecTime.size(); ++i){
            sum += blockExecTime.get(i);
        }

        return sum / blockExecTime.size();
    }

    public List<Long> getBlockExecTime(){
        return blockExecTime;
    }
}
