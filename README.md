# Step function for performan the Ophan Backfill

## What is this?

In order to seed Ophan's historical data index with a large amount of
existing pageview data at launch time, we would like to extract that
data from the data lake and insert it into the rollup index.

This repo contains a step function which does the first part of that:
it uses BigQuery to talk to the the data lake, using an SQL query
(contained in the `src/main/resources` directory of this repo). This
query, as well as extracting the actual data, groups that data on
fields that match the historical data index, effecitvely replicating
the 'rollup'.

This data is then output into a CSV file which can be consumed by the
ingester portion of the backfill process.

## Doing a backfill

In order to actually perform a backfill, the only thing you need to do
is initiate an execution of the step function from the [Step
Function's
page](https://eu-west-1.console.aws.amazon.com/states/home?region=eu-west-1#/statemachines/view/arn:aws:states:eu-west-1:021353022223:stateMachine:Ophan-Backfill-Extractor)
in the AWS console.

The input that it is expecting is a JSON object defining the start
date (inclusive) and end date (exclusive) of the pageviews that should
be extracted:

```json
{
  "startDateInc": "2020-04-10",
  "endDateExc": "2020-05-10"
}
```

This will then trigger the backfill, to extract the data from the data
lake, and it will export the results as CSV files in the bucket
`gs://gu-ophan-backfill-prod` (the final output of the step function
will contain the URL that was used to output the data, in the field
`destinationUri`).

## Authentication

In order to access BigQuery, the step function needs to be able to
read the keys for the BigQuery service account from AWS's secure
paramter store, so make sure these keys exist and are up to date (see
`auth.scala`).
