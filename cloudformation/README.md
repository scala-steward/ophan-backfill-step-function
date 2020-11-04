# Cloudformation for Ophan Backfill Step Function

This directory uses Scala-code to define the cloudformation with the
help of the AWS CDK Java library:

  https://docs.aws.amazon.com/cdk/latest/guide/work-with-cdk-java.html
  
# Generating the cloudformation

In general, you shouldn't need to do this as it is configured to be
part of the teamcity & riffraff build and deploy process.

However, if you need to generate the cloudformation either for testing
or debugging, and assuming you have the `cdk` command installed, run:

```
$ cdk synth
```

This will read the `cdk.json` file to determine how to run the code to
produce the cloudformation.

# Seeing what has changed

If you make changes, you can use:

```
$ cdk diff --profile ophan
```

to see what would be modified if the cloudformation was to be
deployed.
