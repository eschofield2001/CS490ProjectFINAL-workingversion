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
        //Can be very similar to original run(), just pick index based off of HRRN value instead of using FIFO
        activeProcesses.add(waitingProc.getProcessList().get(0));
        Object[] timeRow;
        boolean hasProcess = false;
        int currentRow = 0;
        int maxIndex = 0;
        Process proc = null;
        int arrivingProc;
        float waitTime;
        float Numerator;
        float hrrnTime;
        float maxHrrn = 0;

        while(!Main.getIsPaused()){
            while(!waitingProc.getProcessList().isEmpty()){
                //Since it isn't shared, don't need to use lock for this phase. Look through the queue and find a process to run
                while(hasProcess == false && currentRow < activeProcesses.size()){
                    for (int i=0; i< activeProcesses.size();) {
                        waitTime = systemTimer - waitingProc.getProcessList().get(i).getArrivalTime();
                        Numerator = waitTime + waitingProc.getProcessList().get(i).getServiceTime();
                        hrrnTime = Numerator / waitingProc.getProcessList().get(i).getServiceTime();

                        if (hrrnTime > maxHrrn) {
                            maxHrrn = hrrnTime;
                            maxIndex = i;
                        }
                    i++;
                    }

                        if(activeProcesses.get(currentRow).getArrivalTime() <= systemTimer){
                        cpu.setProcess(activeProcesses.get(maxIndex).getProcessID());
                        cpu.setTimeRem(activeProcesses.get(maxIndex).getTimeRem());

                        hasProcess = true;
                        waitingProc.removeRow(activeProcesses.get(maxIndex).getProcessID());
                    }
                    else{
                        currentRow++;
                    }
                }
                currentRow = 0;

                if(hasProcess == false){
                    //No process has arrived yet, sleep and check next time unit
                    try {
                        Thread.sleep(Main.getTimeUnit().getTimeUnit());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    systemTimer++;
                    arrivingProc = waitingProc.getIncomingProcess(systemTimer);
                    if(arrivingProc != -1){
                        activeProcesses.add(waitingProc.getProcessList().get(arrivingProc));
                    }
                }

                //While processor has a process:
                if(hasProcess){
                    //Sleep until process has finished executing
                    if (activeProcesses.get(maxIndex).getTimeRem() > 0){
                        if (!Main.getIsPaused()){
                            try {
                                Thread.sleep(Main.getTimeUnit().getTimeUnit());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            activeProcesses.get(maxIndex).setTimeRem(activeProcesses.get(maxIndex).getTimeRem() - 1);
                            cpu.setTimeRem(activeProcesses.get(maxIndex).getTimeRem());
                            systemTimer++;
                            arrivingProc = waitingProc.getIncomingProcess(systemTimer);
                            if(arrivingProc != -1){
                                activeProcesses.add(waitingProc.getProcessList().get(arrivingProc));
                            }
                        }
                        else{
                            try {
                                Thread.sleep(Main.getTimeUnit().getTimeUnit());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (activeProcesses.get(maxIndex).getTimeRem() == 0){
                        //Process has finished executing, add to process table
                        timeRow = new Object[6];
                        timeRow[0] = activeProcesses.get(maxIndex).getProcessID();
                        timeRow[1] = activeProcesses.get(maxIndex).getArrivalTime();
                        timeRow[2] = activeProcesses.get(maxIndex).getServiceTime();
                        int tat = systemTimer - activeProcesses.get(maxIndex).getArrivalTime();
                        float nTat = (float) tat / activeProcesses.get(maxIndex).getServiceTime();
                        timeRow[3] = systemTimer;
                        timeRow[4] = tat;
                        timeRow[5] = nTat;
                        totalntat += nTat;

                        finishedProc.getModel().addRow(timeRow);
                        procFinished++;
                        ntatDisplay.setAvg(procFinished, totalntat);
                        activeProcesses.remove(maxIndex);
                    }
                    /*else{
                        //Process didn't finish executing, add to back of process queue
                        waitingProc.getProcessList().add(activeProcesses.get(0));
                        Object[] row = new Object[2];
                        row[0] = activeProcesses.get(0).getProcessID();
                        row[1] = activeProcesses.get(0).getTimeRem();
                        waitingProc.getModel().addRow(row);

                        proc = activeProcesses.get(0);
                        activeProcesses.remove(0);
                        activeProcesses.add(proc);
                    }*/
                    hasProcess = false;
                }
            }
        }
    }
}

