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
public interface CentralityDetection {
    public double getCentrality(double[][]matrixEgoNetwork);
    public CentralityDetection replicate();
}