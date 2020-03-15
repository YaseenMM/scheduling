import java.util.Scanner;
import java.util.*;
import java.io.*;
import java.nio.channels.ReadPendingException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

class lab2{
    public static int randomOS(Scanner Scanner, int U){
        int number = Integer.parseInt(Scanner.next());
        return 1 + (number % U); 
    };
    
    public static class Process implements Comparable<Process>{
        int proNum; 
        int arrivalTime;
        int B;
        int CPUTime;
        int M;
        int remainingCPUTime;
        int finishingTime;
        int turnaroundTime;
        int IOTime; 
        int waitingTime;
        int currCPUB;
        int currIOB = -1;
        int lastCPUB;
        String state = "unstarted";
        int quantum = 2;

        public Process(int A, int B, int C, int M){
            this.arrivalTime = A;
            this.B = B;
            this.CPUTime = C;
            this.remainingCPUTime = C;
            this.M = M;
        }
        @Override
        public int compareTo(Process p){
            return this.arrivalTime - p.arrivalTime;
        }
    };
    public static ArrayList<Process> allProcesses = new ArrayList<Process>();   
    public static ArrayList<Process> allProcesses2 = new ArrayList<Process>();
    public static ArrayList<Process> allProcesses3 = new ArrayList<Process>();
    public static void main (String[] args) throws FileNotFoundException {
        boolean isVerbose;
        if (args.length < 1 || (args[0] == "--verbose" && args.length < 2)){
            System.out.println("No file was inputted...program will now terminate.");
            System.exit(1);
        };
        File text;
        File randomNumbers = new File("random-numbers");
        Scanner numberScanner = new Scanner(randomNumbers);
        String numbers = "";
        String line;
        while (numberScanner.hasNextLine()){
            line = numberScanner.nextLine();
            numbers += line + "\n";
        };
        numberScanner.close();
        Scanner trueNumberScanner = new Scanner (numbers);
        Scanner lcfsNumScanner = new Scanner (numbers);
        Scanner rrNumScanner = new Scanner (numbers);
        if (args.length > 1){
            text = new File(args[1]);
            isVerbose = true;
        }
        else{
            text = new File(args[0]);
            isVerbose = false;
        };
        Scanner userIn = new Scanner (text);
        int numberOfProcesses = userIn.nextInt();
        for (int i = 0; i < numberOfProcesses; i++){
            String g = userIn.next();
            int A = Integer.parseInt(g.substring(1));
            int B = userIn.nextInt();
            int C = userIn.nextInt();
            String q = userIn.next();
            int M = Integer.parseInt(q.substring(0,q.length() - 1));
            Process p = new Process(A, B, C, M);
            allProcesses.add(p);
        }
        userIn.close();
        fcfs(numberOfProcesses, allProcesses, trueNumberScanner, isVerbose);
        for (int i = 0; i < allProcesses.size(); i++){
            Process p = new Process(allProcesses.get(i).arrivalTime, allProcesses.get(i).B, allProcesses.get(i).CPUTime, allProcesses.get(i).M);
            allProcesses2.add(p);
        }
        rr(numberOfProcesses, allProcesses2, rrNumScanner, isVerbose);
        for (int i = 0; i < allProcesses.size(); i++){
            Process p = new Process(allProcesses.get(i).arrivalTime, allProcesses.get(i).B, allProcesses.get(i).CPUTime, allProcesses.get(i).M);
            allProcesses3.add(p);
        }
        lcfs(numberOfProcesses, allProcesses3, lcfsNumScanner, isVerbose);

    }
    public static void fcfs(int NOP, ArrayList<Process> all, Scanner numberScanner, boolean isVerbose){
        ArrayList<Process> terminatedProcesses = new ArrayList<Process>();
        ConcurrentLinkedQueue<Process> readyProcesses = new ConcurrentLinkedQueue<Process>();
        ArrayList<Process> blockedProcesses = new ArrayList<Process>();
        ConcurrentLinkedQueue<Process> processesAsQueue = new ConcurrentLinkedQueue<Process>();
        Process currentProcess = null;
        int cycleNumber = 0;
        int totalCPUCycles = 0;
        int totalIOCycles = 0; 
        String og = "The original input was: " + String.valueOf(NOP) + " ";
        for (int i = 0; i < all.size(); i++){
            og += "(";
            og += String.valueOf(all.get(i).arrivalTime) + " ";
            og += String.valueOf(all.get(i).B) + " ";
            og += String.valueOf(all.get(i).CPUTime) + " ";
            og += String.valueOf(all.get(i).M) + ") ";
        }
        System.out.println(og);
        Collections.sort(all);
        String sorted = "The (sorted) input is: " + String.valueOf(NOP) + " ";
        for (int k = 0; k < all.size(); k++){
            sorted += "(";
            sorted += String.valueOf(all.get(k).arrivalTime) + " ";
            sorted += String.valueOf(all.get(k).B) + " ";
            sorted += String.valueOf(all.get(k).CPUTime) + " ";
            sorted += String.valueOf(all.get(k).M) + ") ";
        }
        System.out.println(sorted + "\n");
        if (isVerbose){
            System.out.println("This detailed printout gives the state and remaining burst for each process \n");
        }
        ArrayList<Process> copy = new ArrayList<Process>();
        for (int g = 0; g < all.size(); g++){
            Process p = all.get(g);
            p.proNum = g;
            copy.add(p);
        };
        Comparator<Process> compareByProNum = (Process p1, Process p2) -> {return Integer.compare(p1.proNum, p2.proNum);};
        Collections.sort(copy, compareByProNum);
        for (int d = 0; d < copy.size(); d++){
            processesAsQueue.add(copy.get(d));
        }
        while (terminatedProcesses.size() < NOP){
            if(isVerbose){
                String verbProcess = "Before cycle " + cycleNumber + ": ";
                System.out.print(verbProcess);
                for (int e = 0; e < all.size(); e++){
                    int allBurst;
                    if (all.get(e).state.equals("blocked")){
                        allBurst = all.get(e).currIOB;
                    }
                    else if (all.get(e).state.equals("running")){
                        allBurst = all.get(e).currCPUB; 
                    }
                    else{
                        allBurst = 0;
                    }
                    String statusString = all.get(e).state + " " + Integer.toString(allBurst);
                    System.out.print(statusString + " ");
                }
                System.out.println("\n");
            }
            if (blockedProcesses.size() > 0){
                ArrayList<Process> exitedIOPhase = new ArrayList<Process>();
                totalIOCycles += 1;
                for (int b = 0; b < blockedProcesses.size(); b++){
                    blockedProcesses.get(b).IOTime += 1;
                    blockedProcesses.get(b).currIOB -= 1;
                    if (blockedProcesses.get(b).currIOB == 0){
                        blockedProcesses.get(b).state = "ready";
                        exitedIOPhase.add(blockedProcesses.get(b));
                    }
                }
                Collections.sort(exitedIOPhase);
                for (int a = 0; a < exitedIOPhase.size(); a++){
                    readyProcesses.add(exitedIOPhase.get(a));
                }
                blockedProcesses.removeAll(exitedIOPhase);
            }
            if (currentProcess != null){
                currentProcess.remainingCPUTime -= 1;
                currentProcess.currCPUB -= 1;
                totalCPUCycles += 1;
                if (currentProcess.remainingCPUTime == 0){
                    currentProcess.state = "terminated";
                    currentProcess.finishingTime = cycleNumber;
                    terminatedProcesses.add(currentProcess);
                    currentProcess = null;
                }
                else if (currentProcess.currCPUB == 0){
                    currentProcess.state = "blocked";
                    blockedProcesses.add(currentProcess);
                   // currentProcess.currIOB = randomOS(numberScanner, currentProcess.B);
                    currentProcess.currIOB = currentProcess.lastCPUB * currentProcess.M;
                    currentProcess = null;
                }
                
            }


            while(processesAsQueue.size() > 0 && processesAsQueue.peek().arrivalTime == cycleNumber){
                Process temp = processesAsQueue.poll();
                temp.state = "ready";
                readyProcesses.add(temp);
            };
            if (currentProcess == null && !readyProcesses.isEmpty()){
                Process temp2 = readyProcesses.poll();
                currentProcess = temp2;
                temp2.state = "running";
                currentProcess.currCPUB = randomOS(numberScanner, currentProcess.B);
                currentProcess.lastCPUB = currentProcess.currCPUB;
            }
            cycleNumber += 1;
        }
        Collections.sort(terminatedProcesses, compareByProNum);
        System.out.println("The scheduling algorithm used was First Come First Served.");
        for (int start = 0; start < terminatedProcesses.size(); start++){
            terminatedProcesses.get(start).turnaroundTime = terminatedProcesses.get(start).finishingTime - terminatedProcesses.get(start).arrivalTime;
            terminatedProcesses.get(start).waitingTime = terminatedProcesses.get(start).finishingTime - terminatedProcesses.get(start).CPUTime - terminatedProcesses.get(start).IOTime - terminatedProcesses.get(start).arrivalTime;
            System.out.println("Process " + terminatedProcesses.get(start).proNum + ":");
            System.out.println("\t (A,B,C,M) = (" + Integer.toString(terminatedProcesses.get(start).arrivalTime) + "," + Integer.toString(terminatedProcesses.get(start).B) + "," + Integer.toString((terminatedProcesses.get(start).CPUTime)) + "," + Integer.toString(terminatedProcesses.get(start).M) + ")");
            System.out.println("\t Finishing time: " + Integer.toString(terminatedProcesses.get(start).finishingTime));
            System.out.println("\t Turnaround time: " + Integer.toString(terminatedProcesses.get(start).turnaroundTime));
            System.out.println("\t IO time: " + Integer.toString(terminatedProcesses.get(start).IOTime));
            System.out.println("\t Waiting time: " + Integer.toString(terminatedProcesses.get(start).waitingTime));
            System.out.println();
        }
        float turnaroundSummary = 0;
        float waitSummary = 0;
        for (int s = 0; s < terminatedProcesses.size(); s++){
            int g = terminatedProcesses.get(s).turnaroundTime;
            turnaroundSummary += g;
            int v = terminatedProcesses.get(s).waitingTime;
            waitSummary += v;
        };
        cycleNumber -= 1;
        System.out.println("Summary Data: ");
        System.out.println("\t Finishing time: " + Integer.toString(cycleNumber));
        System.out.println("\t CPU Utilization: " + Float.toString((float)totalCPUCycles/cycleNumber));
        System.out.println("\t IO Utilization: " + Float.toString((float)totalIOCycles/cycleNumber));
        System.out.println("\t Throughput: " + Float.toString(((float)NOP/cycleNumber)*100) + " processes per hundred cycles");
        System.out.println("\t Average turnaround time: " + Float.toString(turnaroundSummary/NOP));
        System.out.println("\t Average waiting time: " + Float.toString(waitSummary/NOP));
        System.out.println("------------------------------------------------------------");
        }
        public static void rr(int NOP, ArrayList<Process> all, Scanner rrNumScanner, boolean isVerbose){
            ArrayList<Process> terminatedProcesses = new ArrayList<Process>();
            ConcurrentLinkedQueue<Process> readyProcesses = new ConcurrentLinkedQueue<Process>();
            ArrayList<Process> blockedProcesses = new ArrayList<Process>();
            ConcurrentLinkedQueue<Process> processesAsQueue = new ConcurrentLinkedQueue<Process>();
            Process currentProcess = null;
            int cycleNumber = 0;
            int totalCPUCycles = 0;
            int totalIOCycles = 0; 
            String og = "The original input was: " + String.valueOf(NOP) + " ";
            for (int i = 0; i < all.size(); i++){
                og += "(";
                og += String.valueOf(all.get(i).arrivalTime) + " ";
                og += String.valueOf(all.get(i).B) + " ";
                og += String.valueOf(all.get(i).CPUTime) + " ";
                og += String.valueOf(all.get(i).M) + ") ";
            }
            System.out.println(og);
            Collections.sort(all);
            String sorted = "The (sorted) input is: " + String.valueOf(NOP) + " ";
            for (int k = 0; k < all.size(); k++){
                sorted += "(";
                sorted += String.valueOf(all.get(k).arrivalTime) + " ";
                sorted += String.valueOf(all.get(k).B) + " ";
                sorted += String.valueOf(all.get(k).CPUTime) + " ";
                sorted += String.valueOf(all.get(k).M) + ") ";
            }
            System.out.println(sorted + "\n");
            if (isVerbose){
                System.out.println("This detailed printout gives the state and remaining burst for each process \n");
            }
            ArrayList<Process> copy = new ArrayList<Process>();
            for (int g = 0; g < all.size(); g++){
                Process p = all.get(g);
                p.proNum = g;
                copy.add(p);
            };
            Comparator<Process> compareByProNum = (Process p1, Process p2) -> {return Integer.compare(p1.proNum, p2.proNum);};
            Collections.sort(copy, compareByProNum);
            for (int d = 0; d < copy.size(); d++){
                processesAsQueue.add(copy.get(d));
            }
            while (terminatedProcesses.size() < NOP){
                if(isVerbose){
                    String verbProcess = "Before cycle " + cycleNumber + ": ";
                    System.out.print(verbProcess);
                    for (int e = 0; e < all.size(); e++){
                        int allBurst;
                        if (all.get(e).state.equals("blocked")){
                            allBurst = all.get(e).currIOB;
                        }
                        else if (all.get(e).state.equals("running")){
                            allBurst = all.get(e).currCPUB; 
                        }
                        else{
                            allBurst = 0;
                        }
                        String statusString = all.get(e).state + " " + Integer.toString(allBurst);
                        System.out.print(statusString + " ");
                    }
                    System.out.println("\n");
                }
                if (blockedProcesses.size() > 0){
                    ArrayList<Process> exitedIOPhase = new ArrayList<Process>();
                    totalIOCycles += 1;
                    for (int b = 0; b < blockedProcesses.size(); b++){
                        blockedProcesses.get(b).IOTime += 1;
                        blockedProcesses.get(b).currIOB -= 1;
                        if (blockedProcesses.get(b).currIOB == 0){
                            blockedProcesses.get(b).state = "ready";
                            exitedIOPhase.add(blockedProcesses.get(b));
                        }
                    }
                    Collections.sort(exitedIOPhase);
                    for (int a = 0; a < exitedIOPhase.size(); a++){
                        readyProcesses.add(exitedIOPhase.get(a));
                    }
                    blockedProcesses.removeAll(exitedIOPhase);
                }
                if (currentProcess != null){
                    currentProcess.remainingCPUTime -= 1;
                    currentProcess.currCPUB -= 1;
                    totalCPUCycles += 1;
                    currentProcess.quantum -= 1;
                    if (currentProcess.remainingCPUTime == 0){
                        currentProcess.state = "terminated";
                        currentProcess.finishingTime = cycleNumber;
                        terminatedProcesses.add(currentProcess);
                        currentProcess = null;
                    }
                    else if (currentProcess.currCPUB == 0){
                        currentProcess.state = "blocked";
                        blockedProcesses.add(currentProcess);
                       // currentProcess.currIOB = randomOS(numberScanner, currentProcess.B);
                        currentProcess.currIOB = currentProcess.lastCPUB * currentProcess.M;
                        currentProcess = null;
                    }
                    else if (currentProcess.quantum == 0){
                            currentProcess.state = "ready";
                            readyProcesses.add(currentProcess);
                            currentProcess = null;
                    }
                    
                }
    
    
                while(processesAsQueue.size() > 0 && processesAsQueue.peek().arrivalTime == cycleNumber){
                    Process temp = processesAsQueue.poll();
                    temp.state = "ready";
                    readyProcesses.add(temp);
                };
                if (currentProcess == null && !readyProcesses.isEmpty()){
                    Process temp2 = readyProcesses.poll();
                    currentProcess = temp2;
                    temp2.state = "running";
                    currentProcess.currCPUB = randomOS(rrNumScanner, currentProcess.B);
                    currentProcess.lastCPUB = currentProcess.currCPUB;
                    currentProcess.quantum = 2;
                }
                cycleNumber += 1;
            }
            Collections.sort(terminatedProcesses, compareByProNum);
            System.out.println("The scheduling algorithm used was Round Robbin.");
            for (int start = 0; start < terminatedProcesses.size(); start++){
                terminatedProcesses.get(start).turnaroundTime = terminatedProcesses.get(start).finishingTime - terminatedProcesses.get(start).arrivalTime;
                terminatedProcesses.get(start).waitingTime = terminatedProcesses.get(start).finishingTime - terminatedProcesses.get(start).CPUTime - terminatedProcesses.get(start).IOTime - terminatedProcesses.get(start).arrivalTime;
                System.out.println("Process " + terminatedProcesses.get(start).proNum + ":");
                System.out.println("\t (A,B,C,M) = (" + Integer.toString(terminatedProcesses.get(start).arrivalTime) + "," + Integer.toString(terminatedProcesses.get(start).B) + "," + Integer.toString((terminatedProcesses.get(start).CPUTime)) + "," + Integer.toString(terminatedProcesses.get(start).M) + ")");
                System.out.println("\t Finishing time: " + Integer.toString(terminatedProcesses.get(start).finishingTime));
                System.out.println("\t Turnaround time: " + Integer.toString(terminatedProcesses.get(start).turnaroundTime));
                System.out.println("\t IO time: " + Integer.toString(terminatedProcesses.get(start).IOTime));
                System.out.println("\t Waiting time: " + Integer.toString(terminatedProcesses.get(start).waitingTime));
                System.out.println();
            }
            float turnaroundSummary = 0;
            float waitSummary = 0;
            for (int s = 0; s < terminatedProcesses.size(); s++){
                int g = terminatedProcesses.get(s).turnaroundTime;
                turnaroundSummary += g;
                int v = terminatedProcesses.get(s).waitingTime;
                waitSummary += v;
            };
            cycleNumber -= 1;
            System.out.println("Summary Data: ");
            System.out.println("\t Finishing time: " + Integer.toString(cycleNumber));
            System.out.println("\t CPU Utilization: " + Float.toString((float)totalCPUCycles/cycleNumber));
            System.out.println("\t IO Utilization: " + Float.toString((float)totalIOCycles/cycleNumber));
            System.out.println("\t Throughput: " + Float.toString(((float)NOP/cycleNumber)*100) + " processes per hundred cycles");
            System.out.println("\t Average turnaround time: " + Float.toString(turnaroundSummary/NOP));
            System.out.println("\t Average waiting time: " + Float.toString(waitSummary/NOP));
            System.out.println("------------------------------------------------------------");
        }
        public static void lcfs(int NOP, ArrayList<Process> all, Scanner lcfsNumScanner, boolean isVerbose){
            int cycleNumber = 0;
            int totalCPUCycles = 0;
            int totalIOCycles = 0; 
            ArrayList<Process> terminatedProcesses = new ArrayList<Process>();
            Stack<Process> readyProcesses = new Stack<Process>();
            ArrayList<Process> blockedProcesses = new ArrayList<Process>();
            Stack<Process> processesAsStack = new Stack<Process>();
            Process currentProcess = null;
            String og = "The original input was: " + String.valueOf(NOP) + " ";
            for (int i = 0; i < all.size(); i++){
                og += "(";
                og += String.valueOf(all.get(i).arrivalTime) + " ";
                og += String.valueOf(all.get(i).B) + " ";
                og += String.valueOf(all.get(i).CPUTime) + " ";
                og += String.valueOf(all.get(i).M) + ") ";
            }
            System.out.println(og);
            Collections.sort(all);
            String sorted = "The (sorted) input is: " + String.valueOf(NOP) + " ";
            for (int k = 0; k < all.size(); k++){
                sorted += "(";
                sorted += String.valueOf(all.get(k).arrivalTime) + " ";
                sorted += String.valueOf(all.get(k).B) + " ";
                sorted += String.valueOf(all.get(k).CPUTime) + " ";
                sorted += String.valueOf(all.get(k).M) + ") ";
            }
            System.out.println(sorted + "\n");
            if (isVerbose){
                System.out.println("This detailed printout gives the state and remaining burst for each process \n");
            }
            ArrayList<Process> copy = new ArrayList<Process>();
            for (int g = 0; g < all.size(); g++){
                Process p = all.get(g);
                p.proNum = g;
                p.state = "unstarted";
                copy.add(p);
            };
            Collections.sort(copy, Collections.reverseOrder());
            for (int d = 0; d < copy.size(); d++){
                processesAsStack.push(copy.get(d));
            }
            while (terminatedProcesses.size() < NOP){
                ArrayList<Process> removeFromBlock = new ArrayList<Process>();
                if(isVerbose){
                    String verbProcess = "Before cycle " + cycleNumber + ": ";
                    System.out.print(verbProcess);
                    for (int e = 0; e < all.size(); e++){
                        int allBurst;
                        if (all.get(e).state.equals("blocked")){
                            allBurst = all.get(e).currIOB;
                        }
                        else if (all.get(e).state.equals("running")){
                            allBurst = all.get(e).currCPUB; 
                        }
                        else{
                            allBurst = 0;
                        }
                        String statusString = all.get(e).state + " " + Integer.toString(allBurst);
                        System.out.print(statusString + " ");
                    }
                    System.out.println("\n");
                }
                if (blockedProcesses.size() > 0){
                    Stack<Process> exitedIOPhase = new Stack<Process>();
                    totalIOCycles += 1;
                    for (int b = 0; b < blockedProcesses.size(); b++){
                        blockedProcesses.get(b).IOTime += 1;
                        blockedProcesses.get(b).currIOB -= 1;
                        if (blockedProcesses.get(b).currIOB == 0){
                            blockedProcesses.get(b).state = "ready";
                            exitedIOPhase.push(blockedProcesses.get(b));
                        }
                    }
                    for (int a = 0; a < exitedIOPhase.size(); a++){
                        removeFromBlock.add(exitedIOPhase.get(a));
                    }
                    blockedProcesses.removeAll(exitedIOPhase);
                }
                if (currentProcess != null){
                    currentProcess.remainingCPUTime -= 1;
                    currentProcess.currCPUB -= 1;
                    totalCPUCycles += 1;
                    if (currentProcess.remainingCPUTime == 0){
                        currentProcess.state = "terminated";
                        currentProcess.finishingTime = cycleNumber;
                        terminatedProcesses.add(currentProcess);
                        currentProcess = null;
                    }
                    else if (currentProcess.currCPUB == 0){
                        currentProcess.state = "blocked";
                        blockedProcesses.add(currentProcess);
                        currentProcess.currIOB = currentProcess.lastCPUB * currentProcess.M;
                        currentProcess = null;
                    }
                    
                }
                
                while(processesAsStack.size() > 0 && processesAsStack.peek().arrivalTime == cycleNumber){
                    Process temp = processesAsStack.pop();
                    temp.state = "ready";
                    removeFromBlock.add(temp);
                   
                };
                Comparator<Process> doubleSort = (Process p1, Process p2) ->{
                    if (p1.arrivalTime == p2.arrivalTime){
                        return Integer.compare(p2.proNum, p1.proNum);
                    }
                    else{
                        return Integer.compare(p2.arrivalTime, p1.arrivalTime);
                    }
                };
                Collections.sort(removeFromBlock, doubleSort);
                for (int i = 0; i < removeFromBlock.size(); i++){
                   readyProcesses.push(removeFromBlock.get(i));
                }
                
                if (currentProcess == null && !readyProcesses.isEmpty()){
                    if(cycleNumber == 0 || !readyProcesses.isEmpty()){
                        Process temp2 = readyProcesses.pop();
                        currentProcess = temp2;
                        temp2.state = "running";
                        currentProcess.currCPUB = randomOS(lcfsNumScanner, currentProcess.B);
                        currentProcess.lastCPUB = currentProcess.currCPUB;
                    }
                }
                cycleNumber += 1;
            }
            Comparator<Process> compareByProNum = (Process p1, Process p2) -> {return Integer.compare(p1.proNum, p2.proNum);};
            Collections.sort(terminatedProcesses, compareByProNum);
            System.out.println("The scheduling algorithm used was Last Come First Served.");
            for (int start = 0; start < terminatedProcesses.size(); start++){
                terminatedProcesses.get(start).turnaroundTime = terminatedProcesses.get(start).finishingTime - terminatedProcesses.get(start).arrivalTime;
                terminatedProcesses.get(start).waitingTime = terminatedProcesses.get(start).finishingTime - terminatedProcesses.get(start).CPUTime - terminatedProcesses.get(start).IOTime - terminatedProcesses.get(start).arrivalTime;
                System.out.println("Process " + terminatedProcesses.get(start).proNum + ":");
                System.out.println("\t (A,B,C,M) = (" + Integer.toString(terminatedProcesses.get(start).arrivalTime) + "," + Integer.toString(terminatedProcesses.get(start).B) + "," + Integer.toString((terminatedProcesses.get(start).CPUTime)) + "," + Integer.toString(terminatedProcesses.get(start).M) + ")");
                System.out.println("\t Finishing time: " + Integer.toString(terminatedProcesses.get(start).finishingTime));
                System.out.println("\t Turnaround time: " + Integer.toString(terminatedProcesses.get(start).turnaroundTime));
                System.out.println("\t IO time: " + Integer.toString(terminatedProcesses.get(start).IOTime));
                System.out.println("\t Waiting time: " + Integer.toString(terminatedProcesses.get(start).waitingTime) + "\n");
                System.out.println();
            }
            float turnaroundSummary = 0;
            float waitSummary = 0;
            for (int s = 0; s < terminatedProcesses.size(); s++){
                int g = terminatedProcesses.get(s).turnaroundTime;
                turnaroundSummary += g;
                int v = terminatedProcesses.get(s).waitingTime;
                waitSummary += v;
            };
            cycleNumber -= 1;
            System.out.println("Summary Data: ");
            System.out.println("\t Finishing time: " + Integer.toString(cycleNumber));
            System.out.println("\t CPU Utilization: " + Float.toString((float)totalCPUCycles/cycleNumber));
            System.out.println("\t IO Utilization: " + Float.toString((float)totalIOCycles/cycleNumber));
            System.out.println("\t Throughput: " + Float.toString(((float)NOP/cycleNumber)*100) + " processes per hundred cycles.");
            System.out.println("\t Average turnaround time: " + Float.toString(turnaroundSummary/NOP));
            System.out.println("\t Average waiting time: " + Float.toString(waitSummary/NOP));
            System.out.println("------------------------------------------------------------");
        }
}