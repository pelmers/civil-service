package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.validation.groups.DateOfBirthGroup;

import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class Party {

    private String partyID;
    private Type type;
    private String individualTitle;
    private String individualFirstName;
    private String individualLastName;

    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private LocalDate individualDateOfBirth;
    private String companyName;
    private String organisationName;
    private String soleTraderTitle;
    private String soleTraderFirstName;
    private String soleTraderLastName;
    private String soleTraderTradingAs;

    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private LocalDate soleTraderDateOfBirth;
    private Address primaryAddress;

    private String partyName;
    private String partyTypeDisplayValue;

    private String partyEmail;
    private String partyPhone;

    private List<Element<UnavailableDate>> unavailableDates;

    private Flags flags;

    public enum Type {
        INDIVIDUAL,
        COMPANY,
        ORGANISATION,
        SOLE_TRADER;

        public String getDisplayValue() {
            return StringUtils.capitalize(this.name().toLowerCase().replace('_', ' '));
        }
    }

    public String getPartyName(boolean omitTitle) {
        return getPartyNameBasedOnType(this, omitTitle);
    }

    public String getPartyName() {
        return getPartyName(false);
    }

    public String getPartyTypeDisplayValue() {
        return this.getType().getDisplayValue();
    }
}
