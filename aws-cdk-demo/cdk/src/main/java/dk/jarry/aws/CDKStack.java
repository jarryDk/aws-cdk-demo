package dk.jarry.aws;

import java.util.List;
import java.util.Map;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.dynamodb.CfnTable;
import software.amazon.awscdk.services.dynamodb.CfnTable.AttributeDefinitionProperty;
import software.amazon.awscdk.services.dynamodb.CfnTable.KeySchemaProperty;
import software.amazon.awscdk.services.dynamodb.CfnTable.ProvisionedThroughputProperty;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.CfnLayerVersion;
import software.amazon.awscdk.services.lambda.CfnLayerVersion.ContentProperty;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.LambdaInsightsVersion;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.s3.CfnBucket;
import software.amazon.awscdk.services.s3.CfnBucket.PublicAccessBlockConfigurationProperty;
import software.constructs.Construct;

public class CDKStack extends Stack {

	private static final String ENVIRONMENT = "demo";

	static Map<String, String> configuration = Map.of("message", "hello,duke");
	static String functionName = "dk_jarry_aws_lambda";
	static String lambdaHandler = "dk.jarry.aws.lambda.greetings.boundary.Greetings::onEvent";
	static int memory = 128;
	static int timeout = 10;
	static int maxConcurrency = 2;

	/**
	 * <code>
	 * Creates
	 * - DynamoDb table 'aws-cdk-demo-todos
	 * - Role 'aws-cdk-demo-todo-role'
	 * - Bucket 'aws-cdk-demo-lamda-layers'
	 * - Lambda layer 'aws-cdk-demo-java17layer'
	 * </code>
	 *
	 * @param scope
	 * @param id
	 * @param props
	 */
	public CDKStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		CfnTable table = createCfnTable(id);
		Tags.of(table).add("environment", ENVIRONMENT);

		IRole lambdaRole = createRole(id);
		Tags.of(lambdaRole).add("environment", ENVIRONMENT);

		var createBucketDeployment = createCfnBucket();
		Tags.of(createBucketDeployment).add("environment", ENVIRONMENT);

		/**
		 * We only like to do the layer deployment if we have java17layer.zip in the
		 * bucket aws-cdk-demo-lamda-layers
		 */
		boolean doLayerDeployment = Boolean.parseBoolean(System.getenv("DO_LAYER_DEPLOYMENT"));
		if (doLayerDeployment) {
			CfnLayerVersion cfnLayerVersion = createCfnLayerVersion();
			Tags.of(cfnLayerVersion).add("environment", ENVIRONMENT);
		}

		IFunction function = createFunction(functionName, lambdaHandler, configuration, memory, maxConcurrency, timeout,
				lambdaRole);
		CfnOutput.Builder.create(this, "function-output").value(function.getFunctionArn()).build();

	}

	CfnTable createCfnTable(String id) {

		String tableName = id + "-todos";

		return CfnTable.Builder.create(this, tableName) //
				.tableName(tableName) //
				.keySchema( //
						List.of(KeySchemaProperty.builder() //
								.attributeName("uuid") //
								.keyType("HASH") //
								.build()))
				.attributeDefinitions( //
						List.of(AttributeDefinitionProperty.builder() //
								.attributeName("uuid") //
								.attributeType("S") //
								.build()))
				.provisionedThroughput( //
						ProvisionedThroughputProperty.builder() //
								.readCapacityUnits(1) //
								.writeCapacityUnits(1) //
								.build())
				.build();
	}

	IRole createRole(final String id) {

		String roleName = id + "-todo-role";

		Role lambdaRole = Role.Builder.create(this, roleName) //
				.assumedBy(new ServicePrincipal("lambda.amazonaws.com")) //
				.description(id + " role ... with policy to use DynamoDb and Logs") //
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

	/**
	 * https://docs.aws.amazon.com/cdk/api/v2/java/software/amazon/awscdk/services/lightsail/CfnBucket.html
	 * https://docs.aws.amazon.com/cdk/v2/guide/assets.html
	 */
	CfnBucket createCfnBucket() {

		return CfnBucket.Builder //
				.create(this, "CfnBucket") //
				.bucketName("aws-cdk-demo-lamda-layers") //
				.publicAccessBlockConfiguration( //
						PublicAccessBlockConfigurationProperty.builder() //
								.blockPublicAcls(true) //
								.blockPublicPolicy(true) //
								.ignorePublicAcls(true) //
								.restrictPublicBuckets(true) //
								.build())
				.build();
	}

	/**
	 * https://docs.aws.amazon.com/cdk/api/v2/java/software/amazon/awscdk/services/lambda/CfnLayerVersion.html
	 * https://eu-central-1.console.aws.amazon.com/lambda/home?region=eu-central-1#/layers
	 */
	CfnLayerVersion createCfnLayerVersion() {

		return CfnLayerVersion.Builder //
				.create(this, "Java17CfnLayerVersion")
				.content(ContentProperty.builder() //
					.s3Bucket("aws-cdk-demo-lamda-layers") //
					.s3Key("java17layer.zip") //
					.build())
				.description("Java 17 for aws-cdk-demo") //
				.layerName("aws-cdk-demo-java17layer") //
				.licenseInfo("https://www.apache.org/licenses/LICENSE-2.0.txt") //
				.build();
	}

	IFunction createFunction(String functionName, String functionHandler, Map<String, String> configuration, int memory,
			int maximumConcurrentExecution, int timeout, IRole lambdaRole) {

		return Function.Builder //
				.create(this, functionName) //
				.runtime(Runtime.JAVA_11) //
				.code(Code.fromAsset("../target/function.jar")) //
				.handler(functionHandler) //
				.memorySize(memory) //
				.functionName(functionName) //
				.environment(configuration) //
				.timeout(Duration.seconds(timeout)) //
				.insightsVersion(LambdaInsightsVersion.VERSION_1_0_98_0) //
				.tracing(Tracing.ACTIVE) //
				.reservedConcurrentExecutions(maximumConcurrentExecution) //
				.role(lambdaRole) //
				.build();
	}
}
