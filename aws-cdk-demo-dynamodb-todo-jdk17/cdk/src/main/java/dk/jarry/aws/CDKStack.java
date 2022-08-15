package dk.jarry.aws;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.ILayerVersion;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.LayerVersionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

public class CDKStack extends Stack {

	/**
	 * true -> integrates with Http API (quarkus-amazon-lambda-http) false ->
	 * integrates with REST API (quarkus-amazon-lambda-rest)
	 */
	final static boolean HTTP_API_GATEWAY_INTEGRATION = true;

	final static String FUNCTION_NAME = "dk_jarry_lambda_todo_boundary_todos_jdk17";
	final static String LAMBDA_HANDLER = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
	final static int MEMORY = 512;
	final static int MAX_CONCURRENCY = 2;
	final static int TIME_OUT = 10;

	public CDKStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		Map<String, String> configuration = new HashMap<>();

		String dynamoDbTableName = id + "-todos";
		ITable table = findOrCreateTable(id, dynamoDbTableName);
		Tags.of(table).add("environment", "demo");
		configuration.put("dynamoDbTableName", table.getTableName());

		String roleName = id + "-todo-role";
		IRole lambdaRole = findOrCreateRole(id, roleName);

		List<ILayerVersion> java17layer = singletonList(findOrCreateJava17Layer(id));

		Function function = createFunction(configuration, lambdaRole, java17layer);
		Tags.of(function).add("environment", "demo");

		CfnOutput.Builder.create(this, "FunctionArn").value(function.getFunctionArn()).build();

		if (HTTP_API_GATEWAY_INTEGRATION) {
			integrateWithHTTPApiGateway(function);
		} else {
			integrateWithRestApiGateway(function);
		}

	}

	ITable findOrCreateTable(String id, String tableName) {

		ITable table = Table.fromTableName(this, id + "-table", "aws-cdk-demo-todos");
		if (table != null) {
			return table;
		}

		table = Table.Builder.create(this, "todos-in-stack") //
				.tableName(tableName) //
				.partitionKey(Attribute.builder().name("uuid").type(AttributeType.STRING).build()) //
				.readCapacity(1) //
				.writeCapacity(1) //
				.billingMode(BillingMode.PROVISIONED) //
				.build();

		return table;
	}

	IRole findOrCreateRole(final String id, String roleName) {

		IRole fromRoleName = Role.fromRoleName(this, id + "-role", "aws-cdk-demo-todo-role");
		if (fromRoleName != null) {
			return fromRoleName;
		}

		Role lambdaRole = Role.Builder.create(this, "ToDo-Lambda-Role") //
				.assumedBy(new ServicePrincipal("lambda.amazonaws.com")) //
				.description("ToDo role ... with policy to use DynamoDb and Logs") //
				.roleName(roleName) //
				.build();

		lambdaRole.addToPolicy( //
				PolicyStatement.Builder.create() // Restrict to listing and describing tables
						.actions( //
								List.of( //
										"dynamodb:DescribeTable", //
										"dynamodb:ListTables", //
										"dynamodb:PutItem", //
										"dynamodb:GetItem", //
										"dynamodb:UpdateItem", //
										"dynamodb:DeleteItem", //
										"dynamodb:Scan", //
										"logs:CreateLogGroup", //
										"logs:CreateLogStream", //
										"logs:PutLogEvents") //
						) //
						.resources(List.of("*")) //
						.build());

		return lambdaRole;
	}

	ILayerVersion findOrCreateJava17Layer(String id) {

		String account = this.getAccount();
		String layerVersionArn = "arn:aws:lambda:eu-central-1:" + account + ":layer:aws-cdk-demo-java17layer:6";
		ILayerVersion fromLayerVersionArn = LayerVersion.fromLayerVersionArn(this, "aws-cdk-demo-java17layer",
				layerVersionArn);
		if (fromLayerVersionArn != null) {
			return fromLayerVersionArn;
		}

		LayerVersion java17layer = new LayerVersion(this, "Java17Layer", LayerVersionProps.builder() //
				.layerVersionName(id + "-java17-layer") //
				.description("Java 17") //
				.compatibleRuntimes(Arrays.asList(Runtime.PROVIDED_AL2)) //
				.code(Code.fromAsset("../../java17layer.zip")) //
				.build());
		return java17layer;
	}

	Function createFunction(Map<String, String> configuration, IRole lambdaRole, List<ILayerVersion> java17layer) {

		return Function.Builder.create(this, FUNCTION_NAME) //
				.runtime(Runtime.PROVIDED_AL2) //
				.layers(java17layer) //
				.code(Code.fromAsset("../target/function.zip")) //
				.handler(LAMBDA_HANDLER) //
				.memorySize(MEMORY) //
				.functionName(FUNCTION_NAME) //
				.environment(configuration) //
				.timeout(Duration.seconds(TIME_OUT)) //
				.reservedConcurrentExecutions(MAX_CONCURRENCY) //
				.role(lambdaRole) //
				.build();
	}
	
	/**
	 * https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api.html
	 */
	void integrateWithHTTPApiGateway(IFunction function) {
		var lambdaIntegration = HttpLambdaIntegration.Builder.create("HttpApiGatewayIntegration", function).build();
		var httpApiGateway = HttpApi.Builder.create(this, "HttpApiGatewayIntegration")
				.defaultIntegration(lambdaIntegration).build();
		var url = httpApiGateway.getUrl();
		url = url.substring(0, url.length() - 1);
		CfnOutput.Builder.create(this, "HttpApiGatewayUrlOutput").value(url).build();
		CfnOutput.Builder.create(this, "HttpApiGatewayCurlOutput").value("curl -i " + url + "/todos").build();
		CfnOutput.Builder.create(this, "HttpApiGatewayTestOutput").value("../create_todo.sh " + url + "/todos").build();
		CfnOutput.Builder.create(this, "HttpApiGatewaySTOutput")
				.value("mvn compile quarkus:dev -Dquarkus.rest-client.extensions-api.url=" + url).build();
	}

	/**
	 * https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-rest-api.html
	 */
	void integrateWithRestApiGateway(IFunction function) {
		var apiGateway = LambdaRestApi.Builder.create(this, "RestApiGateway").handler(function).build();
		var url = apiGateway.getUrl();
		url = url.substring(0, url.length() - 1);
		CfnOutput.Builder.create(this, "RestApiGatewayUrlOutput").value(url).build();
		CfnOutput.Builder.create(this, "RestApiGatewayCurlOutput").value("curl -i " + url + "/todos").build();
		CfnOutput.Builder.create(this, "RestApiGatewayTestOutput").value("../create_todo.sh " + url + "/todos").build();
		CfnOutput.Builder.create(this, "RestApiGatewaySTOutput")
				.value("mvn compile quarkus:dev -Dquarkus.rest-client.extensions-api.url=" + url).build();
	}

}
