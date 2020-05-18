/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzySelfish;

import net.sourceforge.jFuzzyLogic.FIS;
//import net.sourceforge.jFuzzyLogic.rule.FuzzyRuleSet;

/**
 * Test parsing an FCL file
 * @author pcingola@users.sourceforge.net
 */
public class selfishFuzzy {
    public static void main(String[] args) throws Exception {
        // Load from 'FCL' file
        String fileName = "tipper.fcl";
        FIS fis = FIS.load(fileName,true);

        // Error while loading?
        if( fis == null ) { 
            System.err.println("Can't load file: '" + fileName + "'");
            return;
        }

        // Show 
//        JFuzzyChart.get().chart(functionBlock);

        // Set inputs
        fis.setVariable("service", 3);
        fis.setVariable("food", 7);

        // Evaluate
        fis.evaluate();

        // Show output variable's chart
//        Variable tip = functionBlock.getVariable("tip");
//        JFuzzyChart.get().chart(tip, tip.getDefuzzifier(), true);

        // Print ruleSet
        System.out.println(fis);
    }
}