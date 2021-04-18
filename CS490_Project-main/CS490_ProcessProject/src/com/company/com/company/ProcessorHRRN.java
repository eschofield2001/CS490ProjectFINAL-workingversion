package com.company;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

public class ProcessorHRRN extends Processor{
    private ArrayList<Process> activeProcesses;
    /**
     * Creates the executor and initializes the threadLock as well as sets the CPUPanel to be updated during process execution
     *
     * @param cpu A CPUPanel that displays the CPU
     * @param threadLock A lock to be used when accessing shared variables
     * @param waitingProc A queue of processes waiting to be executed
     * @param finishedProc A table that displays information about the finished processes
     * @param finishedLock might not need
     * @param ntatDisplay A display for the average normalized turnaround time
     */
    public ProcessorHRRN(CPUPanel cpu, Lock threadLock, WaitingQueue waitingProc, FinishedTable finishedProc, Lock finishedLock, NTATDisplay ntatDisplay) {
        super(cpu, threadLock, waitingProc, finishedProc, finishedLock, ntatDisplay);
        activeProcesses = new ArrayList<Process>();
    }

    /**
     * Overrides run in Processor super class to implement the HRRN algorithm
     */
    public void run(){
        int time = 0;
        Object[] timeRow = new Object[0];
        boolean hasProcess = false;
        float waitTime;
        float Numerator;
        float hrrnTime;
        float maxHrrn = 0;
        int maxIndex = 0;

        while(!waitingProc.getProcessList().isEmpty()){
            //Get index of process with highest HRRN
            maxIndex = 0;
            for (int i=0; i< waitingProc.getProcessList().size(); i++) {
                waitTime = systemTimer - waitingProc.getProcessList().get(i).getArrivalTime();
                Numerator = waitTime + waitingProc.getProcessList().get(i).getServiceTime();
                hrrnTime = Numerator / waitingProc.getProcessList().get(i).getServiceTime();

                if (hrrnTime > maxHrrn) {
                    maxHrrn = hrrnTime;
                    maxIndex = i;
                }
            }

            //Check that the process next in line has actually "arrived". If not, sleep for a time unit and check again.
            if(waitingProc.getProcessList().get(maxIndex).getArrivalTime() <= systemTimer) {
                cpu.setProcess(waitingProc.getProcessList().get(maxIndex).getProcessID());
                cpu.setTimeRem(waitingProc.getProcessList().get(maxIndex).getServiceTime());
                time = waitingProc.getProcessList().get(maxIndex).getServiceTime();

                //Initialize table
                timeRow = new Object[6];
                timeRow[0] = waitingProc.getProcessList().get(maxIndex).getProcessID();
                timeRow[1] = waitingProc.getProcessList().get(maxIndex).getArrivalTime();
                timeRow[2] = waitingProc.getProcessList().get(maxIndex).getServiceTime();

                //Update process table and queue
                waitingProc.removeRow(maxIndex);
                hasProcess = true;
            }
            else{
                //Process hasn't arrived yet, sleep and wait for next time unit
                try {
                    Thread.sleep(Main.getTimeUnit().getTimeUnit());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                systemTimer++;
            }


            //Execute the process one second at a time, checking each second if the system is paused and pausing execution if it is
            if(hasProcess) {
                for (int j = time; j > 0; j--) {
                    if (Main.getIsPaused()) {
                        //Do nothing if paused
                        try {
                            Thread.sleep(Main.getTimeUnit().getTimeUnit());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        j++;
                    } else {
                        //Sleep for a second and update timer
                        try {
                            Thread.sleep(Main.getTimeUnit().getTimeUnit());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        cpu.setTimeRem(j-1);
                        systemTimer++;
                    }

                }
                //Calculate table values
                int taT = systemTimer - (Integer) timeRow[1];
                float nTaT = (float) taT / (Integer) timeRow[2];
                timeRow[3] = systemTimer;
                timeRow[4] = taT;
                timeRow[5] = nTaT;
                totalntat += nTaT;

                //Add timeRow to the finished process table
                finishedTableLock.lock();
                try{
                    finishedProc.getModel().addRow(timeRow);
                } finally{
                    finishedTableLock.unlock();
                }

                hasProcess = false;
                procFinished++;
                ntatDisplay.setAvg(procFinished, totalntat);
            }

        }
    }
}

