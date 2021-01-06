package com.gql.microservices.users;

import com.gql.microservices.users.deploy.Runner;

import java.util.Arrays;

public class Manager {
    private static String env;

    public static void main(String[] args) {
        System.out.println("Running users microservices manager with args:\n" + Arrays.toString(args));

        if (args.length < 1) {
            throw new IllegalArgumentException("Arguments not passed correctly");
        }

        env = args[0];
        Runner deployRunner = new Runner();
        deployRunner.synth();
        deployRunner.start();
    }

    public static String withEnv(String name) {
        final String PROJECT_NAME = "gqlh";
        return String.format("%s-%s-%s", PROJECT_NAME, name, env);
    }
}
