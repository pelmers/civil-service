package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CLAIM_ISSUED_DATE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildClaimantReference;

@SpringBootTest(classes = {
    ClaimContinuingOnlineApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class ClaimContinuingOnlineApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private ClaimContinuingOnlineApplicantNotificationHandler handler;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline()).thenReturn("template-id");
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineCos()).thenReturn("template-id-cos");
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                ISSUED_ON, formatLocalDate(CLAIM_ISSUED_DATE, DATE),
                NOTIFICATION_DEADLINE, formatLocalDate(caseData.getClaimNotificationDeadline().toLocalDate(), DATE),
                PARTY_REFERENCES, buildClaimantReference(caseData)
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenCosEnabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id-cos",
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorWithOldTemplate_whenCosDisabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);
            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
        }

    }
}
