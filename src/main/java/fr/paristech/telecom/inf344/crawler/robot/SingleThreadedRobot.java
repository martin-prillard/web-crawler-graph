package fr.paristech.telecom.inf344.crawler.robot;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public abstract class SingleThreadedRobot extends Robot {

    protected SingleThreadedRobot(String ua, long delay) {
        super(ua, delay);
        candidates = new PriorityQueue<String>(initialQueueSize, this.comparator());
        done = new HashSet<String>(initialQueueSize);
    }

    @Override
    public void executionLoop(Set<String> seed,long seconds)
    {
        super.executionLoop(seed, seconds);
        
        long startTime = System.currentTimeMillis();
        
        while((System.currentTimeMillis() - startTime) < seconds*1000) {
            String url = candidates.poll();
            if (url == null) {
                System.out.println("NO MORE CANDIDATES !");
                break; //dance
            }
            processURL(url);
        }
        System.out.println("TIMEOUT !");
    }

}
