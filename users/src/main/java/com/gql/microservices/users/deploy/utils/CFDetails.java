package com.gql.microservices.users.deploy.utils;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CloudFormationException;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Output;
import software.amazon.awssdk.services.cloudformation.model.Stack;

public class CFDetails {
    private static CloudFormationClient cloudFormationClient;

    public static String getUserPoolArn(String stackName) {
        try {
            CloudFormationClient cloudFormation = getCloudFormationClient();
            DescribeStacksResponse describeStacksResponse = cloudFormation.describeStacks(DescribeStacksRequest.builder()
                    .stackName(stackName)
                    .build());

            if (describeStacksResponse.stacks().isEmpty()) {
                System.out.println(String.format("Stack %s is not found.", stackName));
                return "";
            }

            Stack stackDescription = describeStacksResponse.stacks().get(0);
            Output userPoolOutput = stackDescription.outputs().stream()
                    .filter(output -> output.outputKey().equals("UserPoolArn"))
                    .findFirst().orElse(null);

            if (userPoolOutput == null) {
                System.out.println(String.format("User pool id is missing in stack %s.", stackName));
                return "";
            }

            return userPoolOutput.outputValue();
        } catch (CloudFormationException e) {
            return "";
        }
    }

    /**
     * Create or return existing CloudFormation client
     */
    public static CloudFormationClient getCloudFormationClient() {
        if (cloudFormationClient == null) {
            cloudFormationClient = CloudFormationClient.builder().build();
        }
        return cloudFormationClient;
    }
}
