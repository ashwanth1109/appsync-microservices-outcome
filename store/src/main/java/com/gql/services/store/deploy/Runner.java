package com.gql.services.store.deploy;

import com.gql.services.store.Manager;
import com.gql.services.store.deploy.stacks.ServiceStack;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.cxapi.CloudAssembly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Runner {
    private String synthDirectory;

    /**
     * Build the CF template configuration needed for backend deployment Returns the directory where the template config
     * is stored.
     */
    public void synth() {
        App app = new App();
        String rootStack = Manager.withEnv("store-service-stack");
        new ServiceStack(app, rootStack);
        CloudAssembly cs = app.synth();
        synthDirectory = cs.getDirectory();
    }

    /**
     * Deploy the users microservice
     */
    public void start() {
        String[] deployCommand = new String[]
                {"cdk", "--require-approval", "never", "--app", synthDirectory, "deploy" };
        executeCommandAndGetOutput(deployCommand);
    }

    /**
     * Execute a command and gets its console output
     */
    private void executeCommandAndGetOutput(String[] commandAndArguments) {
        try {
            List<String> cmdWithArgs = new ArrayList<>();
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");
            if (isWindows) {
                cmdWithArgs.add("cmd.exe");
                cmdWithArgs.add("/C");
            }
            cmdWithArgs.addAll(Arrays.asList(commandAndArguments));
            ProcessBuilder pb = new ProcessBuilder(cmdWithArgs);
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();
        } catch (InterruptedException | IOException exception) {
            System.err.println("Error in Deploy.executeCommandAndGetOutput");
            exception.printStackTrace();
        }
    }
}
