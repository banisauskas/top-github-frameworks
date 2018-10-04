package service;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import domain.FrontRepository;

/**
 * Tests for "/top" operations.
 *
 * "starredByMe" value is null for not authenticated requests,
 * and not null for authenticated.
 *
 * GitHub has a rate limit of 60 requests per hour for unauthenticated
 * users. When the limit is exceeded, GitHub returns "403 Forbidden".
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=Application.class)
@WebAppConfiguration
public class TopTest {

	// Not null values automatically enable tests, which use authentication
	private static final String USERNAME = null;
	private static final String PASSWORD = null;

	/** JSON converter */
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@Before
	public void setup() {
		this.mockMvc = webAppContextSetup(webAppContext).build();
		this.objectMapper = new ObjectMapper();
	}

	@Test
	public void withoutAuthentication_withoutOrdering() throws Exception {
		// given
		RequestBuilder request = MockMvcRequestBuilders
			.get("/top");

		// when
		MvcResult response = mockMvc
			.perform(request)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
			.andReturn();

		// then
		String responseString = response.getResponse().getContentAsString();
		FrontRepository[] repositories = objectMapper.readValue(responseString, FrontRepository[].class);

		assertThat(repositories, arrayWithSize(10));

		for (FrontRepository repository : repositories) {
			assertThat(repository.getName(), not(isEmptyOrNullString()));
			assertThat(repository.getLink(), not(isEmptyOrNullString()));
			assertThat(repository.getContributors(), not(0));
			assertThat(repository.getStarredByMe(), nullValue());
		}
	}

	@Test
	public void withAuthentication_withoutOrdering() throws Exception {
		if (USERNAME == null || PASSWORD == null) {
			return;
		}

		// given
		RequestBuilder request = MockMvcRequestBuilders
			.get("/top")
			.with(httpBasic(USERNAME, PASSWORD));

		// when
		MvcResult response = mockMvc
			.perform(request)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
			.andReturn();

		// then
		String responseString = response.getResponse().getContentAsString();
		FrontRepository[] repositories = objectMapper.readValue(responseString, FrontRepository[].class);

		assertThat(repositories, arrayWithSize(10));

		for (FrontRepository repository : repositories) {
			assertThat(repository.getName(), not(isEmptyOrNullString()));
			assertThat(repository.getLink(), not(isEmptyOrNullString()));
			assertThat(repository.getContributors(), not(0));
			assertThat(repository.getStarredByMe(), not(nullValue()));
		}
	}

	@Test
	public void withoutAuthentication_withAscendingOrdering() throws Exception {
		// given
		RequestBuilder request = MockMvcRequestBuilders
			.get("/top?orderByContribs=asc");

		// when
		MvcResult response = mockMvc
			.perform(request)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
			.andReturn();

		// then
		String responseString = response.getResponse().getContentAsString();
		FrontRepository[] repositories = objectMapper.readValue(responseString, FrontRepository[].class);

		assertThat(repositories, arrayWithSize(10));
		int previousValue = 1; // must be at least 1 contributor

		for (FrontRepository repository : repositories) {
			assertThat(repository.getStarredByMe(), nullValue());
			assertThat(repository.getContributors(), greaterThanOrEqualTo(previousValue));
			previousValue = repository.getContributors();
		}
	}

	@Test
	public void withAuthentication_withDescendingOrdering() throws Exception {
		if (USERNAME == null || PASSWORD == null) {
			return;
		}

		// given
		RequestBuilder request = MockMvcRequestBuilders
			.get("/top?orderByContribs=desc")
			.with(httpBasic(USERNAME, PASSWORD));

		// when
		MvcResult response = mockMvc
			.perform(request)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
			.andReturn();

		// then
		String responseString = response.getResponse().getContentAsString();
		FrontRepository[] repositories = objectMapper.readValue(responseString, FrontRepository[].class);

		assertThat(repositories, arrayWithSize(10));
		int previousValue = Integer.MAX_VALUE;

		for (FrontRepository repository : repositories) {
			assertThat(repository.getStarredByMe(), not(nullValue()));
			assertThat(repository.getContributors(), lessThanOrEqualTo(previousValue));
			previousValue = repository.getContributors();
		}
	}
}