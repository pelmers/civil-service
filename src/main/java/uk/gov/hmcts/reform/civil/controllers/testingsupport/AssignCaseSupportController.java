package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.controllers.testingsupport.model.UnassignUserFromCasesRequestBody;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.AssignCaseService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.Optional;

@Tag(name = "Assign Case Support Controller")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/testing-support",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnExpression("${testing.support.enabled:false}")
public class AssignCaseSupportController {

    private final UserService userService;
    private final OrganisationService organisationService;
    private final CaseAssignmentSupportService assignCaseSupportService;
    private final AssignCaseService assignCaseService;

    @PostMapping(value = {"/assign-case/{caseId}", "/assign-case/{caseId}/{caseRole}"})
    @Operation(summary = "Assign case to user")
    public void assignCase(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                           @PathVariable("caseId") String caseId,
                           @PathVariable("caseRole") Optional<CaseRole> caseRole) {
        assignCaseService.assignCase(authorisation, caseId, caseRole);
    }

    @PostMapping(value = {"/unassign-user", "/unassign-user"})
    @Operation(summary = "Unassign user from cases")
    public void unAssignUserFromCases(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody UnassignUserFromCasesRequestBody requestBody) {
        String userId = userService.getUserInfo(authorisation).getUid();
        String organisationId = organisationService.findOrganisation(authorisation)
            .map(Organisation::getOrganisationIdentifier).orElse(null);
        assignCaseSupportService.unAssignUserFromCases(requestBody.getCaseIds(), organisationId, userId);
    }
}
