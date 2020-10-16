package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

public class WorkflowException extends Exception {

    public WorkflowException(String message) {
        super(message);
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }

}