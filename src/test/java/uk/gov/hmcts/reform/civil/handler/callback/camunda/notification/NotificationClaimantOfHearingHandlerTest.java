package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationClaimantOfHearingHandler.TASK_ID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@SpringBootTest(classes = {
    NotificationClaimantOfHearingHandler.class,
    JacksonAutoConfiguration.class,
})
public class NotificationClaimantOfHearingHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @Autowired
    private NotificationClaimantOfHearingHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplate())
                .thenReturn("test-template-fee-claimant-id");
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v1WithNoFee() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(0)).build())
                .hearingDueDate(null)
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDatePMDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v1WithNoSolicitorReferences() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(SolicitorReferences.builder().build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDatePMDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd2v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.YES)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd2v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.YES)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }
    }

    @NotNull
    private Map<String, String> getNotificationFeeDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "claimantReferenceNumber", "12345", "hearingFee", "£300.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "23-11-2022"
        );
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "claimantReferenceNumber", "12345", "hearingFee", "£0.00",
            "hearingDate", "07-10-2022", "hearingTime", "08:30am", "hearingDueDate", "23-11-2022"
        );
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDatePMDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "claimantReferenceNumber", "", "hearingFee", "£0.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", ""
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_CLAIMANT_HEARING").build()).build())).isEqualTo(TASK_ID_CLAIMANT);
    }
}
