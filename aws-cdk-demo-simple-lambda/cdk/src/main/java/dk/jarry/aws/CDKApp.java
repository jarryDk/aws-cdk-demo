package dk.jarry.aws;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;



public class CDKApp {
    public static void main(final String[] args) {

            var app = new App();
            var appName = "aws-lambda-cdk-plain";
            Tags.of(app).add("project", "aws-cdk-demo");
            Tags.of(app).add("environment","demo");
            Tags.of(app).add("application", appName);

        var stackProps = StackProps.builder()
                        .build();

        new LambdaStack(app, appName, stackProps);
        app.synth();
    }
}
