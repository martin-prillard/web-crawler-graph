package fr.paristech.telecom.inf344.crawler;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fr.paristech.telecom.inf344.crawler.strategy.BFSRobot;


public class ShowURLRobot extends BFSRobot {

    private int index_node = 0;
    
    private Map<String, Integer> edges = new HashMap<String, Integer>();
    private DirectedSparseGraph g = new DirectedSparseGraph();
    private Layout<String, String> layout = new FRLayout<String, String>(g);
    private VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout, new Dimension(1250, 950));
    private JFrame frame = new JFrame();
    
    public ShowURLRobot(String ua, long delay, int limitedDepth) {
        super(ua, delay, limitedDepth);
        
        vv.setBackground(new Color(255,255,255));
        layout.setSize(new Dimension(1200, 900));
        frame.getContentPane().add(vv);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
    @Override
    protected void dealWith(String url, Set<String> s) {
        super.dealWith(url, s);
        
        if (urlsDepths.get(url) <= limitedDepth) {
            
            System.out.println("URL done : " + url);
            
            // VERTEX
            if (!edges.containsKey(url)) {
                edges.put(url, index_node);
            }
            g.addVertex(url);
            index_node += 1;
            
            // EDGES
            if (s != null) {
                for(String ss:s){
                    if (!edges.containsKey(ss)) {
                        edges.put(ss, index_node);
                        index_node += 1;
                    }
                    g.addEdge(edges.get(url) + "-" + edges.get(ss), url, ss);
                }
            }
            Layout<String, String> layout = new FRLayout<String, String>(g);
            vv.setGraphLayout(layout);
            vv.setVertexToolTipTransformer(new ToStringLabeller<String>());
            vv.setEdgeToolTipTransformer(new Transformer<String,String>(){
                public String transform(String e) {
                    return "Edge:"+g.getEndpoints(e).toString();
                }
            });
            SwingUtilities.updateComponentTreeUI(frame);
        }
    }
    
}
