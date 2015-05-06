package fr.paristech.telecom.inf344.crawler.strategy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.paristech.telecom.inf344.crawler.robot.SingleThreadedRobot;

public class DFSRobot extends SingleThreadedRobot {

    protected Map<String, Integer> urlsDepths = new HashMap<String, Integer>();
    
    protected DFSRobot(String ua, long delay) {
        super(ua, delay);
    }

    @Override
    protected Comparator<String> comparator() {
        Comparator<String> urlComparator = new Comparator<String>() {
            public int compare(String url1, String url2) {
                Integer url1Depth = urlsDepths.get(url1);
                Integer url2Depth = urlsDepths.get(url2);
                
                if (url1Depth < url2Depth){
                    return 1;
                } else {
                    return -1;
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
        if (s != null) {
            for(String ss:s){
                if (!done.contains(ss)){
                    if (!urlsDepths.containsKey(ss)) {
                        urlsDepths.put(ss, urlsDepths.get(url)+1);
                    }
                    candidates.add(ss);
                }
            }
        }
    }
}