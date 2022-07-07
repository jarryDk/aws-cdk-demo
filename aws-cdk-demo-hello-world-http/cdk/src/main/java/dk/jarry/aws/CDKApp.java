package dk.jarry.aws;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

public class CDKApp {
    public static void main(final String[] args) {

        var app = new App();
        var appName = "aws-cdk-demo-hello-world-http";
        Tags.of(app).add("project", "aws-cdk-demo");
        Tags.of(app).add("environment", "demo");
        Tags.of(app).add("application", appName);

        var stackProps = StackProps.builder()
        		.stackName(appName + "-stack")
        		.description("Stack for MicroProfile with Quarkus - HttpApi")
                .build();

        new CDKStack(app, appName, stackProps);
        app.synth();
    }
}
