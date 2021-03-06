package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.SELECTED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.support.GeneralOrdersTestHelper.assertGeneralOrdersWereAdequatelyFiltered;
import static uk.gov.hmcts.reform.divorce.support.GeneralOrdersTestHelper.getGeneralOrdersToAdd;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class RetrieveCaseTest extends RetrieveCaseSupport {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/retrieve-case/";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenCaseExists_whenRetrieveCase_thenReturnResponse() throws Exception {
        UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails,
            Pair.of(D_8_PETITIONER_EMAIL, userDetails.getEmailAddress()));
        String caseId = String.valueOf(caseDetails.getId());

        Pair<String, Object> generalOrders = getGeneralOrdersToAdd();
        updateCase(caseId, null, NO_STATE_CHANGE_EVENT_ID, generalOrders);

        Response cosResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(caseId, cosResponse.path(CASE_ID_JSON_KEY));
        assertEquals(TEST_COURT, cosResponse.path(SELECTED_COURT_KEY));
        assertEquals(AWAITING_PAYMENT, cosResponse.path(STATE_CCD_FIELD));
        String responseJson = cosResponse.getBody().asString();
        String responseJsonData = objectMapper.readTree(responseJson)
            .get(DATA)
            .toString();
        String expectedResponse = loadJson(PAYLOAD_CONTEXT_PATH + "divorce-session.json")
            .replace(USER_DEFAULT_EMAIL, userDetails.getEmailAddress());
        JSONAssert.assertEquals(expectedResponse, responseJsonData, false);
        assertGeneralOrdersWereAdequatelyFiltered(responseJsonData);
    }

    @Test
    public void givenMultipleSubmittedCaseInCcd_whenGetCase_thenReturn300() {
        UserDetails userDetails = createCitizenUser();

        submitCase("submit-complete-case.json", userDetails,
            Pair.of(D_8_PETITIONER_EMAIL, userDetails.getEmailAddress()));
        submitCase("submit-complete-case.json", userDetails,
            Pair.of(D_8_PETITIONER_EMAIL, userDetails.getEmailAddress()));

        Response cosResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cosResponse.getStatusCode());
        assertEquals(cosResponse.getBody().asString(), "");
    }
}