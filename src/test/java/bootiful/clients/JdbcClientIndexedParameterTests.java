/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bootiful.clients;
/*
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.jdbc.Customer;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JdbcClientIndexedParameterTests {

	private static final String SELECT_NAMED_PARAMETERS =
			"select id, forename from custmr where id = ? and country = ?";
	private static final String SELECT_NO_PARAMETERS =
			"select id, forename from custmr";

	private static final String UPDATE_NAMED_PARAMETERS =
			"update seat_status set booking_id = null where performance_id = ? and price_band_id = ?";

	private static final String[] COLUMN_NAMES = new String[] {"id", "forename"};


	private Connection connection = mock();

	private DataSource dataSource = mock();

	private PreparedStatement preparedStatement = mock();

	private ResultSet resultSet = mock();

	private DatabaseMetaData databaseMetaData = mock();

	private JdbcClient client = JdbcClient.create(dataSource);

	private List<Object> params = new ArrayList<>();


	@BeforeEach
	public void setup() throws Exception {
		given(dataSource.getConnection()).willReturn(connection);
		given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
		given(preparedStatement.getConnection()).willReturn(connection);
		given(preparedStatement.executeQuery()).willReturn(resultSet);
		given(databaseMetaData.getDatabaseProductName()).willReturn("MySQL");
		given(databaseMetaData.supportsBatchUpdates()).willReturn(true);
	}


	@Test
	public void testQueryWithResultSetExtractor() throws SQLException {
		given(resultSet.next()).willReturn(true);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		params.add(new SqlParameterValue(Types.DECIMAL, 1));
		params.add("UK");
		Customer cust = client.sql(SELECT_NAMED_PARAMETERS).params(params).query(
				rs -> {
					rs.next();
					Customer cust1 = new Customer();
					cust1.setId(rs.getInt(COLUMN_NAMES[0]));
					cust1.setForename(rs.getString(COLUMN_NAMES[1]));
					return cust1;
				});

		assertThat(cust.getId()).as("Customer id was assigned correctly").isEqualTo(1);
		assertThat(cust.getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
		verify(connection).prepareStatement(SELECT_NAMED_PARAMETERS);
		verify(preparedStatement).setObject(1, 1, Types.DECIMAL);
		verify(preparedStatement).setString(2, "UK");
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testQueryWithResultSetExtractorNoParameters() throws SQLException {
		given(resultSet.next()).willReturn(true);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		Customer cust = client.sql(SELECT_NO_PARAMETERS).query(
				rs -> {
					rs.next();
					Customer cust1 = new Customer();
					cust1.setId(rs.getInt(COLUMN_NAMES[0]));
					cust1.setForename(rs.getString(COLUMN_NAMES[1]));
					return cust1;
				});

		assertThat(cust.getId()).as("Customer id was assigned correctly").isEqualTo(1);
		assertThat(cust.getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
		verify(connection).prepareStatement(SELECT_NO_PARAMETERS);
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testQueryWithRowCallbackHandler() throws SQLException {
		given(resultSet.next()).willReturn(true, false);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		params.add(new SqlParameterValue(Types.DECIMAL, 1));
		params.add("UK");
		final List<Customer> customers = new ArrayList<>();
		client.sql(SELECT_NAMED_PARAMETERS).params(params).query(rs -> {
			Customer cust = new Customer();
			cust.setId(rs.getInt(COLUMN_NAMES[0]));
			cust.setForename(rs.getString(COLUMN_NAMES[1]));
			customers.add(cust);
		});

		assertThat(customers).hasSize(1);
		assertThat(customers.get(0).getId()).as("Customer id was assigned correctly").isEqualTo(1);
		assertThat(customers.get(0).getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
		verify(connection).prepareStatement(SELECT_NAMED_PARAMETERS);
		verify(preparedStatement).setObject(1, 1, Types.DECIMAL);
		verify(preparedStatement).setString(2, "UK");
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testQueryWithRowCallbackHandlerNoParameters() throws SQLException {
		given(resultSet.next()).willReturn(true, false);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		final List<Customer> customers = new ArrayList<>();
		client.sql(SELECT_NO_PARAMETERS).query(rs -> {
			Customer cust = new Customer();
			cust.setId(rs.getInt(COLUMN_NAMES[0]));
			cust.setForename(rs.getString(COLUMN_NAMES[1]));
			customers.add(cust);
		});

		assertThat(customers).hasSize(1);
		assertThat(customers.get(0).getId()).as("Customer id was assigned correctly").isEqualTo(1);
		assertThat(customers.get(0).getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
		verify(connection).prepareStatement(SELECT_NO_PARAMETERS);
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testQueryWithRowMapper() throws SQLException {
		given(resultSet.next()).willReturn(true, false);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		params.add(new SqlParameterValue(Types.DECIMAL, 1));
		params.add("UK");
		List<Customer> customers = client.sql(SELECT_NAMED_PARAMETERS).params(params).query(
				(rs, rownum) -> {
					Customer cust = new Customer();
					cust.setId(rs.getInt(COLUMN_NAMES[0]));
					cust.setForename(rs.getString(COLUMN_NAMES[1]));
					return cust;
				}).list();

		assertThat(customers).hasSize(1);
		Customer cust = customers.get(0);
		assertThat(cust.getId()).as("Customer id was assigned correctly").isEqualTo(1);
		assertThat(cust.getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
		verify(connection).prepareStatement(SELECT_NAMED_PARAMETERS);
		verify(preparedStatement).setObject(1, 1, Types.DECIMAL);
		verify(preparedStatement).setString(2, "UK");
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testQueryWithRowMapperNoParameters() throws SQLException {
		given(resultSet.next()).willReturn(true, false);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		Set<Customer> customers = client.sql(SELECT_NO_PARAMETERS).query(
				(rs, rownum) -> {
					Customer cust = new Customer();
					cust.setId(rs.getInt(COLUMN_NAMES[0]));
					cust.setForename(rs.getString(COLUMN_NAMES[1]));
					return cust;
				}).set();

		assertThat(customers).hasSize(1);
		Customer cust = customers.iterator().next();
		assertThat(cust.getId()).as("Customer id was assigned correctly").isEqualTo(1);
		assertThat(cust.getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
		verify(connection).prepareStatement(SELECT_NO_PARAMETERS);
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testQueryForObjectWithRowMapper() throws SQLException {
		given(resultSet.next()).willReturn(true, false);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		params.add(new SqlParameterValue(Types.DECIMAL, 1));
		params.add("UK");

		Customer cust = client.sql(SELECT_NAMED_PARAMETERS).params(params).query(
				(rs, rownum) -> {
					Customer cust1 = new Customer();
					cust1.setId(rs.getInt(COLUMN_NAMES[0]));
					cust1.setForename(rs.getString(COLUMN_NAMES[1]));
					return cust1;
				}).single();

		assertThat(cust.getId()).as("Customer id was assigned correctly").isEqualTo(1);
		assertThat(cust.getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
		verify(connection).prepareStatement(SELECT_NAMED_PARAMETERS);
		verify(preparedStatement).setObject(1, 1, Types.DECIMAL);
		verify(preparedStatement).setString(2, "UK");
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testQueryForStreamWithRowMapper() throws SQLException {
		given(resultSet.next()).willReturn(true, false);
		given(resultSet.getInt("id")).willReturn(1);
		given(resultSet.getString("forename")).willReturn("rod");

		params.add(new SqlParameterValue(Types.DECIMAL, 1));
		params.add("UK");
		AtomicInteger count = new AtomicInteger();

		try (Stream<Customer> s = client.sql(SELECT_NAMED_PARAMETERS).params(params).query(
				(rs, rownum) -> {
					Customer cust1 = new Customer();
					cust1.setId(rs.getInt(COLUMN_NAMES[0]));
					cust1.setForename(rs.getString(COLUMN_NAMES[1]));
					return cust1;
				}).stream()) {
			s.forEach(cust -> {
				count.incrementAndGet();
				assertThat(cust.getId()).as("Customer id was assigned correctly").isEqualTo(1);
				assertThat(cust.getForename()).as("Customer forename was assigned correctly").isEqualTo("rod");
			});
		}

		assertThat(count.get()).isEqualTo(1);
		verify(connection).prepareStatement(SELECT_NAMED_PARAMETERS);
		verify(preparedStatement).setObject(1, 1, Types.DECIMAL);
		verify(preparedStatement).setString(2, "UK");
		verify(resultSet).close();
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testUpdate() throws SQLException {
		given(preparedStatement.executeUpdate()).willReturn(1);

		params.add(1);
		params.add(1);
		int rowsAffected = client.sql(UPDATE_NAMED_PARAMETERS).params(params).update();

		assertThat(rowsAffected).isEqualTo(1);
		verify(connection).prepareStatement(UPDATE_NAMED_PARAMETERS);
		verify(preparedStatement).setObject(1, 1);
		verify(preparedStatement).setObject(2, 1);
		verify(preparedStatement).close();
		verify(connection).close();
	}

	@Test
	public void testUpdateWithTypedParameters() throws SQLException {
		given(preparedStatement.executeUpdate()).willReturn(1);

		params.add(new SqlParameterValue(Types.DECIMAL, 1));
		params.add(new SqlParameterValue(Types.INTEGER, 1));
		int rowsAffected = client.sql(UPDATE_NAMED_PARAMETERS).params(params).update();

		assertThat(rowsAffected).isEqualTo(1);
		verify(connection).prepareStatement(UPDATE_NAMED_PARAMETERS);
		verify(preparedStatement).setObject(1, 1, Types.DECIMAL);
		verify(preparedStatement).setObject(2, 1, Types.INTEGER);
		verify(preparedStatement).close();
		verify(connection).close();
	}

}
*/