package com.company.Processors;

import com.company.*;
import com.company.Displays.*;
import com.company.Process;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

public class ProcessorRR extends Processor{
    private final TimeSliceDisplay timeSlice;
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
    public ProcessorRR(CPUPanel cpu, Lock threadLock, WaitingQueue waitingProc, FinishedTable finishedProc, Lock finishedLock, NTATDisplay ntatDisplay, TimeSliceDisplay tSlice) {
        super(cpu, threadLock, waitingProc, finishedProc, finishedLock, ntatDisplay);
        timeSlice = tSlice;
        activeProcesses = new ArrayList<Process>();
    }

    /**
     * Returns the systemTimer
     * @return int systemTimer
     */
    public int getTime(){
        return systemTimer;
    }

    /**
     * Returns the number of processes executed
     * @return int procFinished
     */
    public int getProcFinished(){
        return procFinished;
    }

    /**
     * Overrides run in Processor super class to implement the RR algorithm
     */
    public void run(){
        activeProcesses.add(waitingProc.getProcessList().get(0));
        Object[] timeRow;
        boolean hasProcess = false;
        int currentRow = 0;
        Process proc = null;
        int arrivingProc;

        while(!Main.getIsPaused()){
            while(!waitingProc.getProcessList().isEmpty()){
                //Since it isn't shared, don't need to use lock for this phase. Look through the queue and find a process to run
                while(hasProcess == false && currentRow < activeProcesses.size()){
                    if(activeProcesses.get(currentRow).getArrivalTime() <= systemTimer){
                        cpu.setProcess(activeProcesses.get(0).getProcessID());
                        cpu.setTimeRem(activeProcesses.get(0).getTimeRem());

                        hasProcess = true;
                        waitingProc.removeRow(activeProcesses.get(0).getProcessID());
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
                    //Sleep either for duration of time slice, or until process has finished executing
                    for(int j = timeSlice.getTimeSlice(); (j > 0) && (activeProcesses.get(0).getTimeRem() > 0); j--){
                        if (!Main.getIsPaused()){
                            try {
                                Thread.sleep(Main.getTimeUnit().getTimeUnit());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            activeProcesses.get(0).setTimeRem(activeProcesses.get(0).getTimeRem() - 1);
                            cpu.setTimeRem(activeProcesses.get(0).getTimeRem());
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
                            j++;
                        }
                    }

                    if (activeProcesses.get(0).getTimeRem() == 0){
                        //Process has finished executing, add to process table
                        timeRow = new Object[6];
                        timeRow[0] = activeProcesses.get(0).getProcessID();
                        timeRow[1] = activeProcesses.get(0).getArrivalTime();
                        timeRow[2] = activeProcesses.get(0).getServiceTime();
                        int tat = systemTimer - activeProcesses.get(0).getArrivalTime();
                        float nTat = (float) tat / activeProcesses.get(0).getServiceTime();
                        timeRow[3] = systemTimer;
                        timeRow[4] = tat;
                        timeRow[5] = nTat;
                        totalntat += nTat;

                        finishedProc.getModel().addRow(timeRow);
                        procFinished++;
                        ntatDisplay.setAvg(procFinished, totalntat);
                        activeProcesses.remove(0);
                    }
                    else{
                        //Process didn't finish executing, add to back of process queue
                        waitingProc.getProcessList().add(activeProcesses.get(0));
                        Object[] row = new Object[2];
                        row[0] = activeProcesses.get(0).getProcessID();
                        row[1] = activeProcesses.get(0).getTimeRem();
                        waitingProc.getModel().addRow(row);

                        proc = activeProcesses.get(0);
                        activeProcesses.remove(0);
                        activeProcesses.add(proc);
                    }
                    hasProcess = false;
                }
            }
        }
    }
}
