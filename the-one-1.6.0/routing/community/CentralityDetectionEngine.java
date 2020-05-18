/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

/**
 *
 * @author Acer
 */
public interface CentralityDetectionEngine {
    
    //mengembalikan nilai global centrality node
    public double getGlobalDegreeCentrality();
    
    //mengembalikan nilai local centrality node
    public double getLocalDegreeCentrality();
    
    
}
