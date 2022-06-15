package com.cerc.utils.reprocessing.bigquery;

import com.google.cloud.bigquery.TableResult;

public interface BigQueryCase {

    public TableResult getData(String[] references) throws InterruptedException;
}
