package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;

@Component
public class WelshNextEventUtil {

    public String storeNextEventAndReturnStopEvent(BooleanSupplier isWelsh, Map<String, Object> caseData, String eventId,
                                                   String welshNextEventId, String stopEvent) {
        return Optional.of(isWelsh.getAsBoolean())
            .filter(Predicate.isEqual(true))
            .map(k -> {
                caseData.put(WELSH_NEXT_EVENT, welshNextEventId);
                return stopEvent; })
            .orElse(eventId);
    }
}
