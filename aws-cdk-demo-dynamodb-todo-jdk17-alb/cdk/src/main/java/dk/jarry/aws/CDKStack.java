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
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.elasticloadbalancingv2.AddApplicationTargetsProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.BaseApplicationListenerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.elasticloadbalancingv2.targets.LambdaTarget;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.ILayerVersion;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.LayerVersionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

public class CDKStack extends Stack {

	final static String FUNCTION_NAME = "dk_jarry_lambda_todo_boundary_todos_jdk17_alb";
	final static String LAMBDA_HANDLER = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
	final static int MEMORY = 512;
	final static int MAX_CONCURRENCY = 2;
	final static int TIME_OUT = 10;

	public CDKStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		Map<String, String> configuration = new HashMap<>();
		configuration.put("message", "AWS Lambda");

		String dynamoDbTableName = id + "-todos";
		ITable table = findOrCreateTable(id, dynamoDbTableName);
		Tags.of(table).add("environment", "demo");
		configuration.put("dynamoDbTableName", table.getTableName());

		String roleName = id + "-todo-role";
		IRole lambdaRole = findOrCreateRole(id, roleName);

		List<ILayerVersion> java17layer = singletonList(findOrCreateJava17Layer(id));

		Function function = createFunction(configuration, lambdaRole, java17layer);
		Tags.of(function).add("environment", "demo");

		Vpc publicVPC = createVpc();
		ApplicationLoadBalancer loadBalancer = createApplicationLoadBalancer(publicVPC, "DkJarryLB");

		ApplicationListener listener = loadBalancer.addListener("Http", //
				BaseApplicationListenerProps.builder() //
						.port(80) //
						.build());

		LambdaTarget lambdaTarget = new LambdaTarget(function);

		listener.addTargets("Lambda", //
				AddApplicationTargetsProps.builder() //
						.targets(List.of(lambdaTarget)) //
						.healthCheck(HealthCheck.builder() //
								.enabled(true) //
								.interval(Duration.seconds(42)) // https://www.dictionary.com/e/slang/42/
								.path("/q/health") //
								.build()) //
						.build());

		CfnOutput.Builder.create(this, "FunctionARN").value(function.getFunctionArn()).build();
		var loadBalancerDNSName = loadBalancer.getLoadBalancerDnsName();
		CfnOutput.Builder.create(this, "LoadBalancerDNSName") //
				.value(loadBalancerDNSName).build();
		CfnOutput.Builder.create(this, "LoadBalancerCurlOutput") //
				.value("curl -i http://" + loadBalancerDNSName + "/todos").build();
		CfnOutput.Builder.create(this, "LoadBalancerTestOutput").value("../create_todo.sh http://" + loadBalancerDNSName + "/todos").build();
		CfnOutput.Builder.create(this, "LoadBalancerSTOutput")
				.value("mvn compile quarkus:dev -Dquarkus.rest-client.extensions-api.url=http://" + loadBalancerDNSName)
				.build();
	}

	ITable findOrCreateTable(String id, String tableName) {

		ITable table = null;
		/*
		ITable table = Table.fromTableName(this, id + "-table", "aws-cdk-demo-todos");
		if (table != null) {
			return table;
		}
 		*/

		table = Table.Builder.create(this, "todos-in-stack") //
				.tableName(tableName) //
				.partitionKey(Attribute.builder().name("uuid").type(AttributeType.STRING).build()) //
				.readCapacity(1) //
				.writeCapacity(1) //
				.billingMode(BillingMode.PROVISIONED) //
				.build();

		return table;
	}

	IRole findOrCreateRole(String id, String roleName) {

		/*
		IRole fromRoleName = Role.fromRoleName(this, id + "-role", "aws-cdk-demo-todo-role");
		if (fromRoleName != null) {
			return fromRoleName;
		}
 		*/

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

		/*
		String account = this.getAccount();
		String layerVersionArn = "arn:aws:lambda:eu-central-1:" + account + ":layer:aws-cdk-demo-java17layer:3";
		ILayerVersion fromLayerVersionArn = LayerVersion.fromLayerVersionArn(this, "aws-cdk-demo-java17layer",
				layerVersionArn);
		if (fromLayerVersionArn != null) {
			return fromLayerVersionArn;
		}
		*/

		LayerVersion java17layer = new LayerVersion(this, "Java17Layer", LayerVersionProps.builder() //
				.layerVersionName(id + "-java17-layer") //
				.description("Java 17") //
				.compatibleRuntimes(Arrays.asList(Runtime.PROVIDED_AL2)) //
				.code(Code.fromAsset("../../java17layer.zip")) //
				.build());
		return java17layer;
	}

	Vpc createVpc() {
		Vpc vpc = Vpc.Builder.create(this, "VPC")//
				.cidr("10.0.0.0/16")//
				.enableDnsHostnames(true)//
				.enableDnsSupport(true) //
				.natGateways(0) //
				.maxAzs(2) //
				.build();
		return vpc;
	}

	ApplicationLoadBalancer createApplicationLoadBalancer(Vpc vpc, String loadBalancerName) {

		ApplicationLoadBalancer applicationLoadBalancer = ApplicationLoadBalancer.Builder.create(this, "ELB")
				.internetFacing(true) //
				.loadBalancerName(loadBalancerName) //
				.vpc(vpc) //
				.build();

		return applicationLoadBalancer;
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

}
