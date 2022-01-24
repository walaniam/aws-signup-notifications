package walaniam.aws.signup;

import org.junit.jupiter.api.Test;
import walaniam.aws.signup.model.SignupRecord;

import static org.assertj.core.api.Assertions.assertThat;

class JsonApiTest {

    private final JsonApi jsonApi = new JsonApi();

    @Test
    void deserializeSnakeCase() {

        String json = "{\n" +
                "\"name\": \"Marcus\",\n" +
                "\"id\": 1589278470,\n" +
                "\"created_at\": \"2020-05-12T16:11:54.000\"\n" +
                "}";

        SignupRecord record = jsonApi.toPojo(json, SignupRecord.class);
        assertThat(record.getName()).isEqualTo("Marcus");
        assertThat(record.getCreatedAt()).isEqualTo("2020-05-12T16:11:54.000");
    }
}