package com.gql.microservices.orders.deploy.stacks;

import com.gql.microservices.orders.deploy.utils.CFDetails;
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
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;

import static com.gql.microservices.orders.Manager.withEnv;

public class MicroServiceStack extends Stack {
    public MicroServiceStack(final Construct parent, String id) {
        super(parent, id);

        String gqlPath = "./src/main/java/com/gql/microservices/orders/backend/graphql/";
        String apiName = withEnv("orders-api");
        Schema schema = Schema.fromAsset(gqlPath + "Order.graphql");

        AuthorizationConfig authorizationConfig = AuthorizationConfig.builder()
                .defaultAuthorization(AuthorizationMode.builder()
                        .authorizationType(AuthorizationType.USER_POOL)
                        .userPoolConfig(UserPoolConfig.builder()
                                .userPool(UserPool.fromUserPoolArn(
                                        this,
                                        withEnv("orders-pool-config"),
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

        String tableName = withEnv("orders-table");
        Attribute pk = Attribute.builder()
                .name("id").type(AttributeType.STRING).build();

        TableProps tableProps = TableProps.builder()
                .tableName(tableName)
                .partitionKey(pk)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        Table table = new Table(this, tableName, tableProps);

        Attribute gsi1 = Attribute.builder()
                .name("userId").type(AttributeType.STRING).build();
        GlobalSecondaryIndexProps gsiProps = GlobalSecondaryIndexProps.builder()
                .indexName("user-index")
                .partitionKey(gsi1)
                .build();
        table.addGlobalSecondaryIndex(gsiProps);

        DynamoDbDataSource ddbDataSource = api.addDynamoDbDataSource("ddbDataSource", table);

        table.grantFullAccess(ddbDataSource);

        ResolverProps allOrders = ResolverProps.builder()
                .api(api)
                .typeName("Query")
                .fieldName("allOrders")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrders.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrders.res.vtl"))
                .build();
        ddbDataSource.createResolver(allOrders);


        ResolverProps addOrder = ResolverProps.builder()
                .api(api)
                .typeName("Mutation")
                .fieldName("addOrder")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addOrder.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.addOrder.res.vtl"))
                .build();
        ddbDataSource.createResolver(addOrder);

        ResolverProps getOrder = ResolverProps.builder()
                .api(api)
                .typeName("Query")
                .fieldName("getOrder")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getOrder.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.getOrder.res.vtl"))
                .build();
        ddbDataSource.createResolver(getOrder);

        ResolverProps allOrdersForUser = ResolverProps.builder()
                .api(api)
                .typeName("Query")
                .fieldName("allOrdersForUser")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrdersForUser.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Query.allOrdersForUser.res.vtl"))
                .build();
        ddbDataSource.createResolver(allOrdersForUser);

        ResolverProps deleteOrder = ResolverProps.builder()
                .api(api)
                .typeName("Mutation")
                .fieldName("deleteOrder")
                .requestMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.deleteOrder.req.vtl"))
                .responseMappingTemplate(MappingTemplate.fromFile(gqlPath + "resolvers/Mutation.deleteOrder.res.vtl"))
                .build();
        ddbDataSource.createResolver(deleteOrder);
    }
}
