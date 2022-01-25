package walaniam.aws.signup.compiling;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import lombok.extern.slf4j.Slf4j;
import walaniam.aws.signup.model.SignupRecord;

import java.util.Collection;

import static walaniam.aws.signup.DateUtils.timestampOf;

@Slf4j
public class Storage {

    private static final String SIGNUPS_TABLE = "Signups";
    private static final long SIGNUP_TTL_DAYS_IN_SECONDS = 3 * 24 * 60 * 60;
    private static final AmazonDynamoDB DYNAMO_DB_CLIENT = AmazonDynamoDBClientBuilder
            .standard()
            .withClientConfiguration(createClientConfiguration())
            .withRegion(Regions.EU_WEST_1)
            .build();

    private static ClientConfiguration createClientConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        return configuration.withConnectionTimeout(20_000);
    }

    private static final DynamoDB DYNAMO_DB = new DynamoDB(DYNAMO_DB_CLIENT);

    public void save(Collection<SignupRecord> records) {
        records.forEach(this::save);
    }

    public void save(SignupRecord record) {
        long createdAtTimestamp = timestampOf(record.getCreatedAt());
        long ttl = createdAtTimestamp / 1000L + SIGNUP_TTL_DAYS_IN_SECONDS;
        Item item = new Item()
                .withPrimaryKey("id", record.getId())
                .withString("name", record.getName())
                .withLong("created_at", createdAtTimestamp)
                .withLong("ttl", ttl);

        log.info("Getting table {}", SIGNUPS_TABLE);
        Table table = DYNAMO_DB.getTable(SIGNUPS_TABLE);

        log.info("Saving {}, ttl={}", record.getId(), ttl);
        table.putItem(new PutItemSpec().withItem(item));

        log.info("Saved {}", record.getId());
    }
}
