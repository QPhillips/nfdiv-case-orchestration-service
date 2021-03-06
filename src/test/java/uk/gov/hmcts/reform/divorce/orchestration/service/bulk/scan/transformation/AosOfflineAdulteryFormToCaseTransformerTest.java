package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

public class AosOfflineAdulteryFormToCaseTransformerTest {

    private final AosOfflineAdulteryFormToCaseTransformer classUnderTest = new AosOfflineAdulteryFormToCaseTransformer();

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(new OcrDataField("UnexpectedName", "UnexpectedValue")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(1),
            hasEntry("bulkScanCaseReference", "test_case_id")
        ));
    }

    @Test
    public void verifyDataIsCorrectlyTransformed() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("RespConfirmReadPetition", "Yes"),
            new OcrDataField("DateRespReceivedDivorceApplication", "10102019"),
            new OcrDataField("RespAOSAdultery", "Yes"),
            new OcrDataField("RespWillDefendDivorce", "No"),
            new OcrDataField("RespJurisdictionAgree", "Yes"),
            new OcrDataField("RespJurisdictionDisagreeReason", ""),
            new OcrDataField("RespLegalProceedingsExist", "Yes"),
            new OcrDataField("RespLegalProceedingsDescription", "Some random disagree reason"),
            new OcrDataField("RespAgreeToCosts", "Yes"),
            new OcrDataField("RespCostsReason", "I want ma money"),
            new OcrDataField("RespStatementOfTruth", "Yes"),
            new OcrDataField("RespStatementofTruthSignedDate", "11102019")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("RespConfirmReadPetition", "Yes"),
            hasEntry("DateRespReceivedDivorceApplication", "10102019"),
            hasEntry("RespAOSAdultery", "Yes"),
            hasEntry("RespWillDefendDivorce", "No"),
            hasEntry("RespJurisdictionAgree", "Yes"),
            hasEntry("RespJurisdictionDisagreeReason", ""),
            hasEntry("RespLegalProceedingsExist", "Yes"),
            hasEntry("RespLegalProceedingsDescription", "Some random disagree reason"),
            hasEntry("RespAgreeToCosts", "Yes"),
            hasEntry("RespCostsReason", "I want ma money"),
            hasEntry("RespStatementOfTruth", "Yes"),
            hasEntry("RespStatementofTruthSignedDate", "11102019")
        ));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id("test_case_id").ocrDataFields(ocrDataFields).build();
    }
}