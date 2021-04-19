package com.company.Displays;

import javax.swing.*;
import java.awt.*;

/**
 * Panel to display the normalized turnaround time of a processor
 */
public class NTATDisplay extends JPanel {
    private JLabel avg;

    /**
     * Initializes the nTAT and displays the GUI
     */
    public NTATDisplay(){
        avg = new JLabel("0.0");

        setLayout(new FlowLayout());
        JLabel nTATText = new JLabel("Current average nTAT: ");
        add(nTATText);
        add(avg);
    }

    /**
     * Updates the nTAT to display the new average
     * @param proc The number of processes completed
     * @param totalNTAT The combined nTAT of the processes
     */
    public void setAvg(int proc, float totalNTAT){
        Float a = totalNTAT / proc;
        avg.setText(a.toString());
    }
}
