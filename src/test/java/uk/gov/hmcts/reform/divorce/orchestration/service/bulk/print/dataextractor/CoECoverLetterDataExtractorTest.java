package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COST_CLAIMED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COURT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.getCourtId;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.isCostsClaimGranted;

public class CoECoverLetterDataExtractorTest {

    public static final String EXPECTED_COURT = "My court";

    @Test
    public void isCostsClaimGrantedReturnsTrue() {
        Map<String, Object> caseData = createCaseData();

        assertThat(isCostsClaimGranted(caseData), is(true));
    }

    @Test
    public void isCostsClaimGrantedReturnsFalse() {
        assertThat(isCostsClaimGranted(new HashMap<>()), is(false));
        assertThat(isCostsClaimGranted(ImmutableMap.of(COST_CLAIMED, NO_VALUE)), is(false));
        assertThat(isCostsClaimGranted(ImmutableMap.of(COST_CLAIMED, "")), is(false));
        assertThat(isCostsClaimGranted(ImmutableMap.of(COST_CLAIMED, "bla")), is(false));
    }

    @Test
    public void getCourtIdReturnsValue() {
        assertThat(getCourtId(createCaseData()), is(EXPECTED_COURT));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCourtIdThrowsInvalidDataForTaskException() {
        getCourtId(new HashMap<>());
    }

    private static Map<String, Object> createCaseData() {
        Map<String, Object> caseData = DatesDataExtractorTest.createCaseData();
        caseData.put(COURT_ID, EXPECTED_COURT);

        return caseData;
    }
}
