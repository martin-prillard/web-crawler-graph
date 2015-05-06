package fr.paristech.telecom.inf344.crawler.robot;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class MultiThreadedRobot extends RobotMT {

    private int nb_thread = 5;
    
    protected MultiThreadedRobot(String ua, long delay) {
        super(ua, delay);
        candidates = new PriorityBlockingQueue<String>(initialQueueSize, this.comparator());
        done = new HashSet<String>(initialQueueSize);
    }

    @Override
    public void executionLoop(Set<String> seed,long seconds)
    {
        super.executionLoop(seed, seconds);
        
        long startTime = System.currentTimeMillis();
      
        
        ExecutorService es = null;
        Set<ProcessUrlThread> setThread = null;
        
        while((System.currentTimeMillis() - startTime) < seconds*1000) {
            es = Executors.newFixedThreadPool(nb_thread);
            setThread = new HashSet<ProcessUrlThread>();
            // approximate size of URLs to treat by thread
            int urlsThreadToTreat = candidates.size() / nb_thread + 1;
            // launch each thread
            for (int i=0; i<nb_thread; i++) {
                Set<String> urls = new HashSet<String>();
                // create set of URLs
                for (int j=0; j<urlsThreadToTreat; j++) {
                    try {
                        String url = (String) candidates.take();
                        if (url != null) {
                            urls.add(url);
                        }
                    } catch (InterruptedException e) {e.printStackTrace();}
                }
                // execute thread
                ProcessUrlThread t = new ProcessUrlThread(this, urls);
                setThread.add(t);
                es.execute(t);
            }
            // wait end of each threads
            es.shutdown();
            try {
                es.awaitTermination((seconds*1000) - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // test if it's finished
            if (candidates.isEmpty()) {
                System.out.println("NO MORE CANDIDATES !");
                break; //dance
            }
        }
        
        // kill all threads
        for (ProcessUrlThread t: setThread) {
            t.stopThread();
        }
        System.out.println("TIMEOUT !");
        
    }
    
    private static class ProcessUrlThread extends Thread {
        private MultiThreadedRobot multiThreadedRobot;
        private Set<String> urls;
        private boolean stop = false;
        
        public ProcessUrlThread(MultiThreadedRobot multiThreadedRobot, Set<String> urls) {
            this.multiThreadedRobot = multiThreadedRobot;
            this.urls = urls;
        }
        
        public void run() {
            for (String url: urls) {
                if (this.stop) {
                    System.out.println("[ THREAD STOPPED ]");
                    break; //dance
                }
                multiThreadedRobot.processURL(url);
            }
        }
        
        public void stopThread() {
            this.stop = true;
        }

    }
    
}
