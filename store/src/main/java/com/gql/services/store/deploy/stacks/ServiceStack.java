package com.gql.services.store.deploy.stacks;

import com.gql.services.store.deploy.utils.CFDetails;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.CfnOutputProps;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.appsync.AuthorizationConfig;
import software.amazon.awscdk.services.appsync.AuthorizationMode;
import software.amazon.awscdk.services.appsync.AuthorizationType;
import software.amazon.awscdk.services.appsync.GraphqlApi;
import software.amazon.awscdk.services.appsync.GraphqlApiProps;
import software.amazon.awscdk.services.appsync.HttpDataSource;
import software.amazon.awscdk.services.appsync.MappingTemplate;
import software.amazon.awscdk.services.appsync.ResolverProps;
import software.amazon.awscdk.services.appsync.Schema;
import software.amazon.awscdk.services.appsync.UserPoolConfig;
import software.amazon.awscdk.services.appsync.UserPoolDefaultAction;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolProps;

import static com.gql.services.store.Manager.withEnv;

public class ServiceStack extends Stack {
    public ServiceStack(final Construct parent, String id) {
        super(parent, id);

        String userPoolName = withEnv("user-pool");
        UserPoolProps userPoolProps = UserPoolProps.builder()
                .userPoolName(userPoolName)
                .build();
        UserPool userPool = new UserPool(this, userPoolName, userPoolProps);
        UserPoolConfig userPoolConfig = UserPoolConfig.builder()
                .defaultAction(UserPoolDefaultAction.ALLOW)
                .userPool(userPool)
                .build();

        new CfnOutput(this, "UserPoolArn", CfnOutputProps.builder()
                .value(userPool.getUserPoolArn()).build());

        String gqlPath = "./src/main/java/com/gql/services/store/backend/graphql/";
        String apiName = withEnv("store-api");
        Schema schema = Schema.fromAsset(gqlPath + "Store.graphql");
        AuthorizationConfig authorizationConfig = AuthorizationConfig.builder()
                .defaultAuthorization(AuthorizationMode.builder()
                        .authorizationType(AuthorizationType.USER_POOL)
                        .userPoolConfig(userPoolConfig)
                        .build())
                .build();
        GraphqlApiProps graphqlApiProps = GraphqlApiProps.builder()
                .name(apiName)
                .schema(schema)
                .authorizationConfig(authorizationConfig)
                .xrayEnabled(true)
                .build();
        GraphqlApi api = new GraphqlApi(this, apiName, graphqlApiProps);

        new CfnOutput(this, "GraphQLAPIURL", CfnOutputProps.builder()
                .value(api.getGraphqlUrl()).build());

        String ordersGqlUrl = CFDetails
                .getGraphqlUrl(withEnv("orders-micro-service-stack"));

        String usersGqlUrl = CFDetails
                .getGraphqlUrl(withEnv("users-micro-service-stack"));

        if (!ordersGqlUrl.isEmpty()) {
            HttpDataSource ordersDataSource = api.addHttpDataSource("OrderMicroServiceApi", ordersGqlUrl);

            ResolverProps allOrders = ResolverProps.builder()
                    .api(api)
                    .typeName("Query")
                    .fieldName("allOrders")
                    .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrders.req.vtl"))
                    .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrders.res.vtl"))
                    .build();
            ordersDataSource.createResolver(allOrders);

            ResolverProps addOrder = ResolverProps.builder()
                    .api(api)
                    .typeName("Mutation")
                    .fieldName("addOrder")
                    .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addOrder.req.vtl"))
                    .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addOrder.res.vtl"))
                    .build();
            ordersDataSource.createResolver(addOrder);

            ResolverProps getOrder = ResolverProps.builder()
                    .api(api)
                    .typeName("Query")
                    .fieldName("getOrder")
                    .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getOrder.req.vtl"))
                    .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getOrder.res.vtl"))
                    .build();
            ordersDataSource.createResolver(getOrder);

            ResolverProps allOrdersForUser = ResolverProps.builder()
                    .api(api)
                    .typeName("Query")
                    .fieldName("allOrdersForUser")
                    .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrdersForUser.req.vtl"))
                    .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrdersForUser.res.vtl"))
                    .build();
            ordersDataSource.createResolver(allOrdersForUser);
        }

        if (!usersGqlUrl.isEmpty()) {
            HttpDataSource usersDataSource = api.addHttpDataSource("UserMicroServiceApi", usersGqlUrl);

            ResolverProps allUsers = ResolverProps.builder()
                    .api(api)
                    .typeName("Query")
                    .fieldName("allUsers")
                    .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allUsers.req.vtl"))
                    .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allUsers.res.vtl"))
                    .build();
            usersDataSource.createResolver(allUsers);

            ResolverProps addUser = ResolverProps.builder()
                    .api(api)
                    .typeName("Mutation")
                    .fieldName("addUser")
                    .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addUser.req.vtl"))
                    .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addUser.res.vtl"))
                    .build();
            usersDataSource.createResolver(addUser);

            ResolverProps getUser = ResolverProps.builder()
                    .api(api)
                    .typeName("Query")
                    .fieldName("getUser")
                    .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getUser.req.vtl"))
                    .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getUser.res.vtl"))
                    .build();
            usersDataSource.createResolver(getUser);
        }

    }
}
