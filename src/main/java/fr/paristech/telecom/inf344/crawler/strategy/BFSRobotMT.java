package fr.paristech.telecom.inf344.crawler.strategy;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import fr.paristech.telecom.inf344.crawler.robot.MultiThreadedRobot;


public abstract class BFSRobotMT extends MultiThreadedRobot {

    protected Map<String, Integer> urlsDepths = new ConcurrentHashMap<String, Integer>();
    protected int limitedDepth = 999999;
    
    protected BFSRobotMT(String ua, long delay, int limitedDepth) {
        super(ua, delay);
        this.limitedDepth = limitedDepth;
    }

    @Override
    protected Comparator<String> comparator() {
        Comparator<String> urlComparator = new Comparator<String>() {
            public int compare(String url1, String url2) {
                Integer url1Depth = urlsDepths.get(url1);
                Integer url2Depth = urlsDepths.get(url2);
                
                if (url1Depth < url2Depth){
                    return -1;
                } else {
                    return 1;
                }
            }
        };
        return urlComparator;
    }

    @Override
    protected void initialize(String url) {
        urlsDepths.put(url, 0);
    }

    @Override
    protected void dealWith(String url, Set<String> s) {
        if (s != null && urlsDepths.get(url) <= limitedDepth) {
            for(String ss:s){
                if (!done.contains(ss)){
                    if (!urlsDepths.containsKey(ss)) {
                        urlsDepths.put(ss, urlsDepths.get(url)+1);
                    }
                    try {
                        candidates.put(ss);
                    } catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }
    }
    
}