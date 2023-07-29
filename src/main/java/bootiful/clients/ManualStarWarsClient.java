package bootiful.clients;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

//@Service
class ManualStarWarsClient {

    private final RestClient restClient;

    ManualStarWarsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    Planet planets(int id) {
        ResponseEntity<Planet> re = this.restClient
                .get()
                .uri("https://swapi.dev/api/planets/{planetId}/?format=json", id)
                .retrieve()
                .toEntity(Planet.class);
        Assert.state(re.getStatusCode().is2xxSuccessful(), "the request was not successful");
        return re.getBody();
    }
}