package com.hiucimon.storage;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by ndb338 on 12/29/16.
 */
public class CommandLine {
    public OutputCatcher stderr;
    public OutputCatcher stdout;
    private Process p;
    private final Runtime myRuntime;
    private final int COMMAND_FAILED=-9999;
    public CommandLine() {
        myRuntime=Runtime.getRuntime();
    }
    public void Kill() {
        if (p!=null) p.destroy();
    }
    public Integer RunCommandWithResults(String cmd) {
        try {
            p=myRuntime.exec(cmd);
            stderr=new OutputCatcher(p.getErrorStream());
            stdout=new OutputCatcher(p.getInputStream());
            stderr.start();
            stdout.start();
            int result = p.waitFor();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } catch (CancellationException e) {
            e.printStackTrace();
        }
        return COMMAND_FAILED;
    }
    public Integer RunCommandWithResults(String cmd, Integer timeout) {
        int result=-9999;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = (Future<Integer>) executor.submit(() -> {
                        //executor.submit(new myThreadClass(this,cmd)
                        return this.RunCommandWithResults(cmd);
                }
        );
        try {
            System.out.println("Started..");
            result=future.get(timeout, TimeUnit.SECONDS);
            if (result==-9999) {
                Kill();
            }
            System.out.println("Finished!");
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Terminated!");
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } catch (CancellationException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();
        return result;
    }
    public static void main(String[] argv) {
        System.out.println("Starting");
        CommandLine c=new CommandLine();
        int r=c.RunCommandWithResults("clamscan -v DoCommandLineWithResults.iml");
        System.out.println("Results="+r);
        c.stdout.getLines().forEach(l->System.out.println("My command returned:  "+l));
        int r2=c.RunCommandWithResults("clamscan -v DoCommandLineWithResults.iml",1);
        System.out.println("Results="+r2);
        c.stdout.getLines().forEach(l->System.out.println("My command returned:  "+l));
    }
}
