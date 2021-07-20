# Ophan backfill: Data lake extractor

-----

## What is this?

In order to seed Ophan's historical data index with a large amount of
existing pageview data at launch time, we would like to extract that
data from the data lake and insert it into the rollup index.

This repo contains a step function which does the first part of that:
it uses BigQuery to talk to the data lake, using an SQL query
(contained in the `src/main/resources` directory of this repo). This
query, as well as extracting the actual data, groups that data on
fields that match the historical data index, effectively replicating
the 'rollup'.

This data is then output into a CSV file which can be consumed by the
ingester portion of the backfill process.

## Extracting pageviews from the data lake

In order to actually perform a backfill, the first thing you need to do
is initiate an execution of the **OphanBackfillExtractorAF53033F-FG1hRg9d46yf** step function from the [Step
Function's
page](https://eu-west-1.console.aws.amazon.com/states/home?region=eu-west-1#/statemachines)
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

This will extract pageviews from the data
lake, exporting the results as CSV files in the bucket
`gs://gu-ophan-backfill-prod`. The final output of the step function
will contain the URL that was used to output the data, in the field
`destinationUri`.

For the step function itself, follow the naming convention `backfill-with-manifest-21jun2021-04jul2021`
to keep things clear.

## Authentication

In order to access BigQuery, the step function needs to be able to
read the keys for the BigQuery service account from AWS's secure
parameter store, so make sure these keys exist and are up to date (see
`auth.scala`).
