package com.cerc.utils.reprocessing.bigquery;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.cerc.utils.reprocessing.contants.Constants;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;

import javax.inject.Inject;

public class BigQueryCaseImpl implements BigQueryCase{

    @Inject
    BigQuery bigquery; // Inject BigQuery

    @Override
    public TableResult getData(String[] references) throws InterruptedException{

        String referencesQuery = toString(references);

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(
                        Constants.query.replace("references",referencesQuery))
                .setUseLegacySql(false)
                .build();

        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        queryJob = queryJob.waitFor();

        // Check for errors
        if (queryJob == null) {
            throw new RuntimeException("Job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        // Get the results and return them
        TableResult result = queryJob.getQueryResults();
        return result;

    }

    public static String toString(String[] array) {
        String result = ""; if (array.length > 0) {
            StringBuilder sb = new StringBuilder();
                for (String s : array) { sb.append(s).append(",");
            }
                result = sb.deleteCharAt(sb.length() - 1).toString();
        } return result;
    }

}
