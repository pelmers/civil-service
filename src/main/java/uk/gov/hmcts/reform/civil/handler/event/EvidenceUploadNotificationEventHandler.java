package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadApplicantNotificationHandler;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadRespondentNotificationHandler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceUploadNotificationEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    private final EvidenceUploadApplicantNotificationHandler evidenceUploadApplicantNotificationHandler;
    private final EvidenceUploadRespondentNotificationHandler evidenceUploadRespondentNotificationHandler;

    /**
     * This method will send notification to applicant and respondent solicitors.
     * This method will not throw any exception but log warning if there is any error while
     * sending notification because these email notifications are not business critical
     * and provided as a courtesy, as the user can log in and see the new uploads.
     *
     * @param event EvidenceUploadNotificationEvent
     */
    @EventListener
    public void sendEvidenceUploadNotification(EvidenceUploadNotificationEvent event) {
        CaseDetails caseDetails = coreCaseDataService.getCase(event.getCaseId());
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        try {
            evidenceUploadApplicantNotificationHandler.notifyApplicantEvidenceUpload(caseData);
        } catch (Exception e) {
            log.warn("Failed to send email notification to applicant for case '{}'", event.getCaseId());
        }
        try {
            evidenceUploadRespondentNotificationHandler.notifyRespondentEvidenceUpload(caseData, true);
        } catch (Exception e) {
            log.warn("Failed to send email notification to respondent solicitor1 for case '{}'", event.getCaseId());
        }
        try {
            evidenceUploadRespondentNotificationHandler.notifyRespondentEvidenceUpload(caseData, false);
        } catch (Exception e) {
            log.warn("Failed to send email notification to respondent solicitor2 for case '{}'", event.getCaseId());
        }
    }
}
