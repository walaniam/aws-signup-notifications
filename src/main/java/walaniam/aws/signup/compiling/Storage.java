package walaniam.aws.signup.compiling;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import lombok.extern.slf4j.Slf4j;
import walaniam.aws.signup.model.SignupRecord;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static walaniam.aws.signup.DateUtils.timestampOf;
import static walaniam.aws.signup.DateUtils.yearMonthOf;

@Slf4j
public class Storage {

    private static final String SIGNUPS_TABLE = "Signups";
    private static final long SIGNUP_TTL_DAYS_IN_SECONDS = 1 * 24 * 60 * 60;
    private static final AmazonDynamoDB DYNAMO_DB_CLIENT = AmazonDynamoDBClientBuilder
            .standard()
            .withClientConfiguration(createClientConfiguration())
            .withRegion(Regions.EU_WEST_1)
            .build();

    private static final DynamoDB DYNAMO_DB = new DynamoDB(DYNAMO_DB_CLIENT);

    public void save(Collection<SignupRecord> records) {
        records.forEach(this::save);
    }

    public void save(SignupRecord record) {

        long createdAtTimestamp = timestampOf(record.getCreatedAt());
        long ttl = createdAtTimestamp / 1000L + SIGNUP_TTL_DAYS_IN_SECONDS;

        Item item = new Item()
                .withPrimaryKey(
                        "year_month_created", yearMonthOf(record.getCreatedAt()),
                        "created_at", createdAtTimestamp
                )
                .withNumber("id", record.getId())
                .withString("name", record.getName())
                .withLong("ttl", ttl);

        log.info("Getting table {}", SIGNUPS_TABLE);
        Table table = DYNAMO_DB.getTable(SIGNUPS_TABLE);

        log.info("Saving {}, ttl={}", record.getId(), ttl);
        table.putItem(new PutItemSpec().withItem(item));

        log.info("Saved {}", record.getId());
    }

    public List<SignupRecord> queryByYearMonthCreated(String yearMonth) {

        Table table = DYNAMO_DB.getTable(SIGNUPS_TABLE);

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#year_month_created = :year_month")
                .withValueMap(new ValueMap().withString(":year_month", yearMonth));

        log.info("Query: {}", querySpec);

        ItemCollection<QueryOutcome> items = table.query(querySpec);

        return StreamSupport.stream(items.spliterator(), false)
                .map(item -> SignupRecord.builder()
                        .id(item.getNumber("id").longValueExact())
                        .name(item.getString("name"))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static ClientConfiguration createClientConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        return configuration.withConnectionTimeout(20_000);
    }
}
