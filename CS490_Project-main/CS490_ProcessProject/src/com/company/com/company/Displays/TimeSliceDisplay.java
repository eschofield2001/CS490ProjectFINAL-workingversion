package com.company.Displays;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that allows the user to enter a new time slice value for RR
 */
public class TimeSliceDisplay extends JPanel{
    int timeSlice;

    /**
     * Initializes the time slice to 1 and displays the GUI
     */
    public TimeSliceDisplay(){
        timeSlice = 1;

        setLayout(new FlowLayout());
        JLabel rrtext = new JLabel("RR Time\n Slice Length ");
        final int FIELD_WIDTH = 10;
        JTextField timeText = new JTextField(FIELD_WIDTH);
        timeText.setText("1");

        //Updates timeUnit when enterButton is pressed
        JButton enterButton = new JButton("Enter");
        enterButton.addActionListener(e -> timeSlice = Integer.parseInt(timeText.getText()));

        add(rrtext);
        add(timeText);
        add(enterButton);
    }

    /**
     * Returns the current time slice value
     * @return The time slice
     */
    public int getTimeSlice(){
        return timeSlice;
    }
}
