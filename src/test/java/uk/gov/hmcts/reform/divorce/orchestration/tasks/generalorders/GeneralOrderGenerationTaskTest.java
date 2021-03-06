package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.DivorceGeneralOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeTypeNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.PETITIONER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateTimeForDocument;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderGenerationTaskTest extends AbstractGeneralOrderGenerationTaskTest {

    @InjectMocks
    private GeneralOrderGenerationTask generalOrderGenerationTask;

    @Before
    public void setup() throws JudgeTypeNotFoundException {
        super.setup();
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        Map<String, Object> caseData = executeShouldGenerateAFile();

        assertThat(caseData, hasKey(GENERAL_ORDERS));
    }

    @Override
    public GeneralOrderGenerationTask getTask() {
        return generalOrderGenerationTask;
    }

    @Override
    protected void runVerifications(Map<String, Object> expectedIncomingCaseData,
                                    Map<String, Object> returnedCaseData,
                                    String expectedDocumentType,
                                    String expectedTemplateId,
                                    DocmosisTemplateVars expectedDocmosisTemplateVars) {
        verifyNewDocumentWasAddedToCaseData(returnedCaseData, expectedDocumentType);
        verifyPdfDocumentGenerationCallIsCorrect(expectedTemplateId, expectedDocmosisTemplateVars);
    }

    private void verifyNewDocumentWasAddedToCaseData(
        Map<String, Object> returnedCaseData, String expectedDocumentType) {
        String expectedGeneralOrderFileName = format(DOCUMENT_FILENAME_FMT, expectedDocumentType, formatDateTimeForDocument(LocalDateTime.now()));

        List<CollectionMember<DivorceGeneralOrder>> generalOrdersCollection = (List) returnedCaseData.get(GENERAL_ORDERS);
        assertThat(generalOrdersCollection.size(), is(1));

        DivorceGeneralOrder item = generalOrdersCollection.get(0).getValue();
        assertThat(item.getDocument().getDocumentType(), is(expectedDocumentType));
        assertThat(item.getDocument().getDocumentFileName(), is(expectedGeneralOrderFileName));

        assertThat(item.getGeneralOrderParties().size(), is(1));
        assertThat(item.getGeneralOrderParties().get(0), is(PETITIONER));
    }

}