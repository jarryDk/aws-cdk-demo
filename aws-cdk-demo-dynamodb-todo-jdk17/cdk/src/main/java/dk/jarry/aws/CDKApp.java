package dk.jarry.aws;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

public class CDKApp {
    public static void main(final String[] args) {

        var app = new App();
        var appName = "aws-cdk-demo-dynamodb-todo-jdk17";
        Tags.of(app).add("project", "MicroProfile with Quarkus on AWS Lambda using Jdk17 - DynamoDB");
        Tags.of(app).add("environment", "demo");
        Tags.of(app).add("application", appName);

        var stackProps = StackProps.builder()
        		.stackName(appName + "-stack")
        		.description("Stack for MicroProfile with Quarkus using Jdk17 - DynamoDB")
                .build();

        new CDKStack(app, appName, stackProps);
        app.synth();
    }
}
