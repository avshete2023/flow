package com.flow.connector.connector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ConnectorValidationModelTest {

    @Test
    void shouldCreateSuccessValidationModel() {
        ConnectorValidationModel result = ConnectorValidationModel.success();

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldCreateFailureValidationModel() {
        ConnectorValidationModel result = ConnectorValidationModel.failure(List.of("Invalid connector configuration"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("Invalid connector configuration");
    }
}

