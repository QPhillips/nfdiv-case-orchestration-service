package uk.gov.hmcts.reform.divorce.orchestration.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IdamApiHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public IdamApiHealthCheck(HttpEntityFactory httpEntityFactory,
                                  @Qualifier("healthCheckRestTemplate") RestTemplate restTemplate,
                                  @Value("${idam.api.url}/health") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}