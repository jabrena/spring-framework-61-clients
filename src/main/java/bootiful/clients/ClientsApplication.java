package bootiful.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.Assert;

import java.io.NotSerializableException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class ClientsApplication {

    private static final Logger logger = LoggerFactory.getLogger(ClientsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ClientsApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(JdbcClient jdbcClient) {
        return args -> {

            insertExample(jdbcClient);
            selectExample(jdbcClient, 1);
            //insertExample2(jdbcClient);
            deleteExample(jdbcClient);
            selectExample(jdbcClient, 0);

//          var kh = new GeneratedKeyHolder(List.of(Map.of("id", Integer.class)));


        };
    }

    private void insertExample(JdbcClient jdbcClient) {

        logger.info("Insert Example");

        var sqlStatement = """
            INSERT INTO planet(name) VALUES(?)
            """;
        var updatedRows = jdbcClient
                .sql(sqlStatement)
                .params(List.of("Hoth"))
                .update();
        Assert.state(updatedRows == 1, "there should have been one or more records updated");
    }

    private void selectExample(JdbcClient jdbcClient, int expectedCount) {

        logger.info("Select Example");

        var sqlStatement = """
            SELECT * FROM planet
            """;
        RowMapper<Planet2> planetRowMapper = (rs, rowNum) -> new Planet2(
                rs.getInt("id"),
                rs.getString("name"));
        var count = jdbcClient.sql(sqlStatement)
                .query(planetRowMapper)
                .list()
                .stream()
                .peek(p -> logger.info("{}", p))
                .count();

        Assert.state(count == expectedCount, "there should have been one or more records updated");
    }

    private void insertExample2(JdbcClient jdbcClient) {

        logger.info("Insert Example 2");

        var sqlStatement = """
            INSERT INTO planet(name) VALUES(?);
            SELECT IDENTITY();
            """;

        try {
            var generatedKeyHolder = new GeneratedKeyHolder();
            var updatedRows = jdbcClient
                    .sql(sqlStatement)
                    .params(List.of("Tatooine"))
                    .update(generatedKeyHolder);

            String id = generatedKeyHolder.getKeyAs(String.class);
            logger.info("Returned Id: {}", id);

            Assert.state(updatedRows == 1, "there should have been one or more records updated");
        } catch (Exception e) {
            logger.error("Katakroker");
            logger.error(e.getLocalizedMessage());
            logger.error(e.getCause().getClass().getName());
        }
    }

    private void deleteExample(JdbcClient jdbcClient) {
        logger.info("Delete Example");
        var sql = "DELETE FROM planet";
        jdbcClient.sql(sql).update();
    }
}
