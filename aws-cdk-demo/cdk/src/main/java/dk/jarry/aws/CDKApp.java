package dk.jarry.aws;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

public class CDKApp {
    public static void main(final String[] args) {

        var app = new App();
        var appName = "aws-cdk-demo";
        Tags.of(app).add("project", "IoC for aws-cdk-demo");
        Tags.of(app).add("environment", "demo");
        Tags.of(app).add("application", appName);

        var stackProps = StackProps.builder()
        		.stackName(appName + "-stack")
        		.description("Stack IoC for aws-cdk-demo")
                .build();

        new CDKStack(app, appName, stackProps);
        app.synth();
    }
}
