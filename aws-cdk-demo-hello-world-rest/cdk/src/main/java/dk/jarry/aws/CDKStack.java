package dk.jarry.aws;

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
import software.amazon.awscdk.services.dynamodb.CfnTable;
import software.amazon.awscdk.services.dynamodb.CfnTable.AttributeDefinitionProperty;
import software.amazon.awscdk.services.dynamodb.CfnTable.KeySchemaProperty;
import software.amazon.awscdk.services.dynamodb.CfnTable.ProvisionedThroughputProperty;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

public class CDKStack extends Stack {

	/**
	 * true -> integrates with Http API (quarkus-amazon-lambda-http)
	 * false -> integrates with REST API (quarkus-amazon-lambda-rest)
	 */
	static boolean HTTP_API_GATEWAY_INTEGRATION = false;

	static Map<String, String> configuration = Map.of("message", "Hello World - Quarkus as AWS Lambda");
	static String functionName = "dk_jarry_aws_boundary_hello_rest";
	static String lambdaHandler = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
	static int memory = 1024; // ~0.5 vCPU
	static int maxConcurrency = 2;
	static int timeout = 10;

	public CDKStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		var function = createFunction(functionName, lambdaHandler, configuration, memory, maxConcurrency, timeout);
		Tags.of(function).add("environment", "demo");

		if (HTTP_API_GATEWAY_INTEGRATION) {
			integrateWithHTTPApiGateway(function);
		} else {
			integrateWithRestApiGateway(function);
		}

	}

	/**
	 * https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api.html
	 */
	void integrateWithHTTPApiGateway(IFunction function) {
		var lambdaIntegration = HttpLambdaIntegration.Builder.create("HttpApiGatewayIntegration", function).build();
		var httpApiGateway = HttpApi.Builder.create(this, "HttpApiGatewayIntegration")
				.defaultIntegration(lambdaIntegration).build();
		var url = httpApiGateway.getUrl();
		CfnOutput.Builder.create(this, "HttpApiGatewayUrlOutput").value(url).build();
		CfnOutput.Builder.create(this, "HttpApiGatewayCurlOutput").value("curl -i " + url + "hello").build();
	}

	/**
	 * https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-rest-api.html
	 */
	void integrateWithRestApiGateway(IFunction function) {
		var apiGateway = LambdaRestApi.Builder.create(this, "RestApiGateway").handler(function).build();
		var url = apiGateway.getUrl();
		CfnOutput.Builder.create(this, "RestApiGatewayUrlOutput").value(url).build();
		CfnOutput.Builder.create(this, "RestApiGatewayCurlOutput").value("curl -i " + url + "hello").build();
	}

	Function createFunction(String functionName, String functionHandler, Map<String, String> configuration, int memory,
			int maximumConcurrentExecution, int timeout) {

		return Function.Builder.create(this, functionName) //
				.runtime(Runtime.JAVA_11) //
				.architecture(Architecture.ARM_64) //
				.code(Code.fromAsset("../target/function.zip")) //
				.handler(functionHandler) //
				.memorySize(memory) //
				.functionName(functionName) //
				.environment(configuration) //
				.timeout(Duration.seconds(timeout)) //
				.build();
	}

}
