package fr.paristech.telecom.inf344.crawler.main;

import java.util.HashSet;
import java.util.Set;

import fr.paristech.telecom.inf344.crawler.GraphExtractionRobot;
import fr.paristech.telecom.inf344.crawler.GraphExtractionRobotMT;
import fr.paristech.telecom.inf344.crawler.ShowURLRobot;
import fr.paristech.telecom.inf344.crawler.ShowURLRobotMT;


public class ShowURL {

    private final static String BFS = "BFS";
    private final static String BFS_MT = "BFS_MT";
    private final static String DFS = "DFS";
    private final static String DFS_MT = "DFS_MT";
    
    public static void main(String [] args)
    {
        long delay = 1;
        long timeout = 10;
        int limitedDepth = 2;
        
        Set<String> seed = new HashSet<String>();
        seed.add("http://www.lemonde.fr/");
        
        
        /* CHANGE IT with : 
         * BFS
         * BFS_MT
         * DFS
         * DFS_MT
         * */
        String selected_mode = BFS;
        
     
        switch (selected_mode)
        {
          // I. Implementing a simple BFS crawler
          case BFS:
              new ShowURLRobot("Martin_Prillard_web_crawler", delay, limitedDepth).executionLoop(seed, timeout);
              break;   
          // Multi Threaded version
          case BFS_MT:
              new ShowURLRobotMT("Martin_Prillard_web_crawler", delay, limitedDepth).executionLoop(seed, timeout);
              break;
          // II. GraphExtractionRobot - DFS
          case DFS:
              new GraphExtractionRobot("Martin_Prillard_web_crawler", delay).executionLoop(seed, timeout);
              break;
          // Multi Threaded version
          case DFS_MT:
              new GraphExtractionRobotMT("Martin_Prillard_web_crawler", delay).executionLoop(seed, timeout);
              break;
        } 
        System.out.println("END");
        
    }

}
