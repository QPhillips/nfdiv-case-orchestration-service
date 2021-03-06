package uk.gov.hmcts.reform.divorce.orchestration.util.payment;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

public class PbaClientErrorMessageTest {

    @Test
    public void shouldGetRightEnumByString() {
        assertThat(PbaErrorMessage.getMessage("CAE0001"), equalTo(PbaErrorMessage.CAE0001));
        assertThat(PbaErrorMessage.getMessage("CAE0004"), equalTo(PbaErrorMessage.CAE0004));
        assertThat(PbaErrorMessage.getMessage("NOTFOUND"), equalTo(PbaErrorMessage.NOTFOUND));
        assertThat(PbaErrorMessage.getMessage("GENERAL"), equalTo(PbaErrorMessage.GENERAL));
    }

    @Test
    public void shouldReturnFullErrorMessageContent() {
        String generalMessage = "Payment request failed. " + PbaErrorMessage.ERROR_INFO;

        assertThat(PbaErrorMessage.GENERAL.value(), is(generalMessage));
    }

    @Test
    public void shouldThrowExceptionWhenMessageIsInvalid() {
        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> PbaErrorMessage.getMessage("Unknown error message")
        );

        assertThat(
            illegalArgumentException.getMessage(),
            is("Unknown error message")
        );
    }
}