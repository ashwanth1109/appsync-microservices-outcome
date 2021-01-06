package com.gql.services.store;

import com.gql.services.store.deploy.Runner;

import java.util.Arrays;

import static com.gql.services.store.deploy.utils.Constants.PROJECT_NAME;

public class Manager {
    private static String env;

    public static void main(String[] args) {
        System.out.println("Running store services manager with args:\n" + Arrays.toString(args));

        if (args.length < 1) {
            throw new IllegalArgumentException("Arguments not passed correctly");
        }

        env = args[0];
        Runner deployRunner = new Runner();
        deployRunner.synth();
        deployRunner.start();
    }

    public static String withEnv(String name) {
        return String.format("%s-%s-%s", PROJECT_NAME, name, env);
    }
}
