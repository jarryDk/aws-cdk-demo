package dk.jarry.aws;

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
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

public class CDKStack extends Stack {
	
	public CDKStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);
		
		var table = createTable(id);
		Tags.of(table).add("environment", "demo");
		
		IRole lambdaRole = createRole(id);
		Tags.of(lambdaRole).add("environment", "demo");
		
	}
	
	ITable createTable(String id) {
		
		String tableName = id + "-todos";

		ITable table = Table.fromTableName(this, id + "-table", tableName);
		if(table != null) {
			return table;
		}

		table = Table.Builder.create(this, "todos-table") //
				.tableName(tableName) //
				.partitionKey(Attribute.builder().name("uuid").type(AttributeType.STRING).build()) //				
				.readCapacity(1) //
				.writeCapacity(1) //				
				.billingMode(BillingMode.PROVISIONED) //
				.build();
				
		return table;
	}

	IRole createRole(final String id) {

		String roleName = id + "-todo-role";
		
		IRole fromRoleName = Role.fromRoleName(this, id + "-role", roleName);		
		if(fromRoleName != null) {
			return fromRoleName;
		}

		Role lambdaRole = Role.Builder.create(this, "todo-role") //
				.assumedBy(new ServicePrincipal("lambda.amazonaws.com")) //
				.description( id + " role ... with policy to use DynamoDb and Logs") //
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

}
