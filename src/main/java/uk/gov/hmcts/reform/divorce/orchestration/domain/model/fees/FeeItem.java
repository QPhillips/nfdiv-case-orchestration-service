package uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FeeItem {

    @JsonProperty("value")
    private FeeValue value;

}
