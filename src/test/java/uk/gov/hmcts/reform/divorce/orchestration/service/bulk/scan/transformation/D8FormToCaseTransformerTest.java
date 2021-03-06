package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class D8FormToCaseTransformerTest {

    private final D8FormToCaseTransformer classUnderTest = new D8FormToCaseTransformer();

    @Test
    public void verifySeparationDateIsTransformed() {
        ExceptionRecord exceptionRecord = createExceptionRecord(singletonList(new OcrDataField("D8ReasonForDivorceSeparationDate", "20/11/2008")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8ReasonForDivorceSeperationDay", "20"),
            hasEntry("D8ReasonForDivorceSeperationMonth", "11"),
            hasEntry("D8ReasonForDivorceSeperationYear", "2008"),
            hasEntry("bulkScanCaseReference", "test_case_id")
        ));
    }

    @Test
    public void shouldReplaceCcdFieldForD8PaymentMethodIfPaymentMethodIsDebitCreditCard() {
        assertFieldValueIsTransformed("D8PaymentMethod", "Debit/Credit Card", is("card"));
    }

    @Test
    public void shouldNotReplaceCcdFieldForD8PaymentMethodIfMethodIsCheque() {
        assertFieldValueIsTransformed("D8PaymentMethod", "Cheque", is("cheque"));
    }

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
    public void shouldReplaceCcdFieldForD8FinancialOrderForIfIsForMyselfMyChildrenOrBoth() {
        assertFieldValueIsTransformed("D8FinancialOrderFor", "myself", hasItem("petitioner"));
        assertFieldValueIsTransformed("D8FinancialOrderFor", "my children", hasItem("children"));
        assertFieldValueIsTransformed("D8FinancialOrderFor", "myself, my children", hasItems("petitioner", "children"));
    }

    @Test
    public void verifyPetitionerHomeAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8PetitionerHomeAddress",
            ImmutableMap.of(
                "D8PetitionerHomeAddressStreet", "19 West Park Road",
                "D8PetitionerHomeAddressTown", "Smethwick",
                "D8PetitionerPostCode", "SE1 2PT",
                "D8PetitionerHomeAddressCounty", "West Midlands"
            ));
    }

    @Test
    public void verifyPetitionerSolicitorAddressIsTransformed() {
        assertAddressIsTransformed(
            "PetitionerSolicitorAddress",
            ImmutableMap.of(
                "PetitionerSolicitorAddressStreet", "20 solicitors road",
                "PetitionerSolicitorAddressTown", "Soltown",
                "PetitionerSolicitorAddressPostCode", "SE1 2PT",
                "PetitionerSolicitorAddressCounty", "East Midlands"
            ));
    }

    @Test
    public void verifyD8PetitionerCorrespondenceAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8PetitionerCorrespondenceAddress",
            ImmutableMap.of(
                "D8PetitionerCorrespondenceAddressStreet", "20 correspondence road",
                "D8PetitionerCorrespondenceAddressTown", "Correspondencetown",
                "D8PetitionerCorrespondencePostcode", "SE12BP",
                "D8PetitionerCorrespondenceAddressCounty", "South Midlands"
            ));
    }

    @Test
    public void verifyD8ReasonForDivorceAdultery3rdPartyAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8ReasonForDivorceAdultery3rdAddress",
            ImmutableMap.of(
                "D8ReasonForDivorceAdultery3rdPartyAddressStreet", "third party street",
                "D8ReasonForDivorceAdultery3rdPartyTown", "Thirdtown",
                "D8ReasonForDivorceAdultery3rdPartyPostCode", "SE3 5PP",
                "D8ReasonForDivorceAdultery3rdPartyCounty", "North Midlands"
            ));
    }

    @Test
    public void verifyMarriageDateIsCorrectlyTransformedGivenSeparateComponents() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8MarriageDateDay", "06"),
            new OcrDataField("D8MarriageDateMonth", "5"),
            new OcrDataField("D8MarriageDateYear", "2007")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8MarriageDate", "2007-05-06"),
            not(hasKey("D8MarriageDateDay")),
            not(hasEntry("D8MarriageDate", "2007-5-6")),
            not(hasEntry("D8MarriageDate", "2007-06-05"))
        ));
    }

    @Test
    public void verifyD8MentalSeparationDateIsCorrectlyTransformedGivenSeparateComponents() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8MentalSeparationDateDay", "06"),
            new OcrDataField("D8MentalSeparationDateMonth", "5"),
            new OcrDataField("D8MentalSeparationDateYear", "2007")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8MentalSeparationDate", "2007-05-06"),
            not(hasKey("D8MentalSeparationDay")),
            not(hasEntry("D8MentalSeparationDate", "2007-5-6")),
            not(hasEntry("D8MentalSeparationDate", "2007-06-05"))
        ));
    }

    @Test
    public void verifyD8PhysicalSeparationDateIsCorrectlyTransformedGivenSeparateComponents() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8PhysicalSeparationDateDay", "06"),
            new OcrDataField("D8PhysicalSeparationDateMonth", "5"),
            new OcrDataField("D8PhysicalSeparationDateYear", "2007")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8PhysicalSeparationDate", "2007-05-06"),
            not(hasKey("D8PhysicalSeparationDateDay")),
            not(hasEntry("D8PhysicalSeparationDate", "2007-5-6")),
            not(hasEntry("D8PhysicalSeparationDate", "2007-06-05"))
        ));
    }

    @Test
    public void verifySection3RespondentIsTransformed() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8PetitionerNameDifferentToMarriageCert", "Yes"),
            new OcrDataField("RespNameDifferentToMarriageCertExplain", "Dog ate the homework"),
            new OcrDataField("D8RespondentEmailAddress", "jack@daily.mail.com"),
            new OcrDataField("D8RespondentCorrespondenceSendToSol", "Yes"),
            new OcrDataField("D8RespondentSolicitorName", "Judge Law"),
            new OcrDataField("D8RespondentSolicitorReference", "JL007"),
            new OcrDataField("D8RespondentSolicitorCompany", "A-Team")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8PetitionerNameDifferentToMarriageCert", "Yes"),
            hasEntry("RespNameDifferentToMarriageCertExplain", "Dog ate the homework"),
            hasEntry("D8RespondentEmailAddress", "jack@daily.mail.com"),
            hasEntry("D8RespondentCorrespondenceSendToSol", "Yes"),
            hasEntry("D8RespondentSolicitorName", "Judge Law"),
            hasEntry("D8RespondentSolicitorReference", "JL007"),
            hasEntry("D8RespondentSolicitorCompany", "A-Team")
        ));
    }

    @Test
    public void shouldTransformFieldsAsExpectedForSection11() {
        //Generic test
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8AppliesForStatementOfTruth", "marriage"),
            new OcrDataField("D8DivorceClaimFrom", "corespondent"),
            new OcrDataField("D8FinancialOrderStatementOfTruth", "petitioner, children"),
            new OcrDataField("D8FullNameStatementOfTruth", "Peter F. Griffin"),
            new OcrDataField("D8StatementofTruthSignature", "Yes"),
            new OcrDataField("D8StatementofTruthDate", "17/01/2020"),
            new OcrDataField("D8SolicitorsFirmStatementOfTruth", "Quahog Solicitors Ltd."),
            new OcrDataField("D8PetitionerNameChangedHowOtherDetails", "new name much change such detail")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8FullNameStatementOfTruth", "Peter F. Griffin"),
            hasEntry("D8StatementOfTruthSignature", "Yes"),
            hasEntry("D8StatementOfTruthDate", "2020-01-17"),
            hasEntry("D8SolicitorsFirmStatementOfTruth", "Quahog Solicitors Ltd."),
            hasEntry("D8PetitionerNameChangedHowOtherDetails", "new name much change such detail")
        ));
        assertValueMatchesIterableMatcher(transformedCaseData, "D8AppliesForStatementOfTruth", hasItems("marriage"));
        assertValueMatchesIterableMatcher(transformedCaseData, "D8DivorceClaimFrom", hasItems("correspondent"));
        assertValueMatchesIterableMatcher(transformedCaseData, "D8FinancialOrderStatementOfTruth", hasItems("petitioner", "children"));

        //More granular tests
        assertFieldValueIsTransformed("D8DivorceClaimFrom", "respondent", hasItem("respondent"));
        assertFieldValueIsTransformed("D8DivorceClaimFrom", "corespondent", hasItem("correspondent"));
        assertFieldValueIsTransformed("D8DivorceClaimFrom", "respondent, corespondent", hasItems("respondent", "correspondent"));
    }

    @Test
    public void verifyD8RespondentHomeAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8RespondentHomeAddress",
            ImmutableMap.of(
                "D8RespondentHomeAddressStreet", "12 Res Pond",
                "D8RespondentHomeAddressTown", "Divorcity",
                "D8RespondentPostcode", "RE5 3NT",
                "D8RespondentHomeAddressCounty", "Mid Midlands"
            ));
    }

    @Test
    public void verifyD8RespondentSolicitorAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8RespondentSolicitorAddress",
            ImmutableMap.of(
                "D8RespondentSolicitorAddressStreet", "50 Licitor",
                "D8RespondentSolicitorAddressTown", "Lawyerpool",
                "D8RespondentSolicitorAddressPostCode", "SO2 7OR",
                "D8RespondentSolicitorAddressCounty", "Higher Midlands"
            ));
    }

    @Test
    public void shouldTurnTheseCommaSeparatedValuesIntoArrays() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8AppliesForStatementOfTruth", "marriage, dissolution"),
            new OcrDataField("D8DivorceClaimFrom", "respondent, corespondent"),
            new OcrDataField("D8FinancialOrderStatementOfTruth", "petitioner, children")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertValueMatchesIterableMatcher(transformedCaseData, "D8AppliesForStatementOfTruth", hasItems("marriage", "dissolution"));
        assertValueMatchesIterableMatcher(transformedCaseData, "D8DivorceClaimFrom", hasItems("respondent", "correspondent"));
        assertValueMatchesIterableMatcher(transformedCaseData, "D8FinancialOrderStatementOfTruth", hasItems("petitioner", "children"));
    }

    @Test
    public void shouldTurnTheseTrueOrFalseValuesIntoYesOrNo() {
        Map<String, Object> transformedCaseDataWithYesValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8ScreenHasMarriageCert", "True"),
            new OcrDataField("D8CertificateInEnglish", "True")
        )));

        assertThat(transformedCaseDataWithYesValue, allOf(
            hasEntry("D8ScreenHasMarriageCert", YES_VALUE),
            hasEntry("D8CertificateInEnglish", YES_VALUE)
        ));

        Map<String, Object> transformedCaseDataWithNoValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8ScreenHasMarriageCert", "False"),
            new OcrDataField("D8CertificateInEnglish", "False")
        )));

        assertThat(transformedCaseDataWithNoValue, allOf(
            hasEntry("D8ScreenHasMarriageCert", NO_VALUE),
            hasEntry("D8CertificateInEnglish", NO_VALUE)
        ));

        Map<String, Object> transformedCaseDataWithEmptyValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8CertificateInEnglish", "")
        )));

        assertThat(transformedCaseDataWithEmptyValue, allOf(
            not(hasKey("D8ScreenHasMarriageCert")),
            hasEntry("D8CertificateInEnglish", "")
        ));
    }

    @Test
    public void shouldTransformContactDetailsConfidentiality() {
        Map<String, Object> transformedCaseDataWithYesValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8PetitionerContactDetailsConfidential", YES_VALUE)
        )));

        assertThat(transformedCaseDataWithYesValue, hasEntry("D8PetitionerContactDetailsConfidential", "keep"));

        Map<String, Object> transformedCaseDataWithNoValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8PetitionerContactDetailsConfidential", NO_VALUE)
        )));

        assertThat(transformedCaseDataWithNoValue, hasEntry("D8PetitionerContactDetailsConfidential", "share"));
    }

    @Test
    public void shouldTransformD8LegalProcessIntoValidValues() {
        Map<String, Object> transformedCaseDataWithDivorceValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8LegalProcess", "Divorce")
        )));
        assertThat(transformedCaseDataWithDivorceValue, hasEntry("D8legalProcess", "divorce"));

        Map<String, Object> transformedCaseDataWithDissolutionValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8LegalProcess", "Dissolution")
        )));
        assertThat(transformedCaseDataWithDissolutionValue, hasEntry("D8legalProcess", "dissolution"));

        Map<String, Object> transformedCaseDataWithJudicialSeparationValue = classUnderTest.transformIntoCaseData(createExceptionRecord(asList(
            new OcrDataField("D8LegalProcess", "Judicial (separation)")
        )));
        assertThat(transformedCaseDataWithJudicialSeparationValue, hasEntry("D8legalProcess", "judicialSeparation"));
    }

    private void assertValueMatchesIterableMatcher(Map<String, Object> transformedCaseData, String fieldName, Matcher iterableMatcher) {
        assertThat(transformedCaseData.get(fieldName), iterableMatcher);
    }

    private void assertFieldValueIsTransformed(String field, String inputValue, Matcher matcher) {
        ExceptionRecord exceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField(field, inputValue)));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData.get(field), matcher);
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id("test_case_id").ocrDataFields(ocrDataFields).build();
    }

    private void assertAddressIsTransformed(String targetParentField, Map<String, String> sourceFieldAndValueMap) {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(sourceFieldAndValueMap.entrySet().stream()
            .map(fieldAndValue -> new OcrDataField(fieldAndValue.getKey(), fieldAndValue.getValue()))
            .collect(Collectors.toList()));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);

        Map<String, String> sourceSuffixToTargetMap = ImmutableMap.of(
            "AddressStreet", "AddressLine1",
            "Town", "PostTown",
            "PostCode", "PostCode",
            "County", "County"
        );
        BiFunction<Map<String, String>, String, String> getValueForSuffix = (map, suffix) -> map.entrySet().stream()
            .filter(entry -> entry.getKey().toLowerCase().endsWith(suffix.toLowerCase()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Expected to find key with suffix %s in map", suffix)))
            .getValue();

        Map parentField = (Map) transformedCaseData.getOrDefault(targetParentField, emptyMap());
        sourceSuffixToTargetMap.forEach((key, value) -> assertThat(parentField.get(value), is(getValueForSuffix.apply(sourceFieldAndValueMap, key))));
    }
}
