package com.gql.microservices.users.deploy.stacks;

import com.gql.microservices.users.deploy.utils.CFDetails;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.CfnOutputProps;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.appsync.AuthorizationConfig;
import software.amazon.awscdk.services.appsync.AuthorizationMode;
import software.amazon.awscdk.services.appsync.AuthorizationType;
import software.amazon.awscdk.services.appsync.DynamoDbDataSource;
import software.amazon.awscdk.services.appsync.GraphqlApi;
import software.amazon.awscdk.services.appsync.GraphqlApiProps;
import software.amazon.awscdk.services.appsync.MappingTemplate;
import software.amazon.awscdk.services.appsync.ResolverProps;
import software.amazon.awscdk.services.appsync.Schema;
import software.amazon.awscdk.services.appsync.UserPoolConfig;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;

import static com.gql.microservices.users.Manager.withEnv;

public class MicroServiceStack extends Stack {
    public MicroServiceStack(final Construct parent, String id) {
        super(parent, id);

        String gqlPath = "./src/main/java/com/gql/microservices/users/backend/graphql/";
        String apiName = withEnv("users-api");
        Schema schema = Schema.fromAsset(gqlPath + "User.graphql");

        AuthorizationConfig authorizationConfig = AuthorizationConfig.builder()
                .defaultAuthorization(AuthorizationMode.builder()
                        .authorizationType(AuthorizationType.USER_POOL)
                        .userPoolConfig(UserPoolConfig.builder()
                                .userPool(UserPool.fromUserPoolArn(
                                        this,
                                        withEnv("users-pool-config"),
                                        CFDetails.getUserPoolArn(withEnv("store-service-stack"))
                                ))
                                .build())
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

        String tableName = withEnv("users-table");
        Attribute pk = Attribute.builder()
                .name("id").type(AttributeType.STRING).build();

        TableProps tableProps = TableProps.builder()
                .tableName(tableName)
                .partitionKey(pk)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        Table table = new Table(this, tableName, tableProps);

        DynamoDbDataSource ddbDataSource = api.addDynamoDbDataSource("ddbDataSource", table);

        ResolverProps allUsers = ResolverProps.builder()
                .api(api)
                .typeName("Query")
                .fieldName("allUsers")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allUsers.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allUsers.res.vtl"))
                .build();
        ddbDataSource.createResolver(allUsers);


        ResolverProps addUser = ResolverProps.builder()
                .api(api)
                .typeName("Mutation")
                .fieldName("addUser")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addUser.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addUser.res.vtl"))
                .build();
        ddbDataSource.createResolver(addUser);

        ResolverProps getUser = ResolverProps.builder()
                .api(api)
                .typeName("Query")
                .fieldName("getUser")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getUser.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getUser.res.vtl"))
                .build();
        ddbDataSource.createResolver(getUser);

        ResolverProps addOrderToUser = ResolverProps.builder()
                .api(api)
                .typeName("Mutation")
                .fieldName("addOrderToUser")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addOrderToUser.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addOrderToUser.res.vtl"))
                .build();
        ddbDataSource.createResolver(addOrderToUser);
    }
}
