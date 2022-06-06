package com.cerc.utils.reprocessing.contants;

public final class Constants {

    private Constants() {}

    public static final String query = "select\n" +
            "\tt1.id,\n" +
            "\tt1.requester,\n" +
            "\tt1.createdAt,\n" +
            "\tt1.receivedAt,\n" +
            "\tt1.workflowMetadata as payload,\n" +
            "\tt1.compressed,\n" +
            "\tt1.requesterTransactionId\n" +
            "from\n" +
            "\tcerc2-book-prd-01.replica_spanner_book_transaction.Transaction t1\n" +
            "inner join (\n" +
            "\tselect\n" +
            "\t\tMAX(t.createdAt) createdAt ,\n" +
            "\t\tt.contractReference\n" +
            "\tfrom\n" +
            "\t\tcerc2-book-prd-01.replica_spanner_book_transaction.Transaction t\n" +
            "\twhere\n" +
            "\t\tt.contractReference in(${references})\n" +
            "\t\tand t.type in (0, 1)\n" +
            "\tgroup by\n" +
            "\t\tt.contractReference \n" +
            "    ) t2 on\n" +
            "\tt1.contractReference = t2.contractReference\n" +
            "\tand t1.createdAt = t2.createdAt;";
}
