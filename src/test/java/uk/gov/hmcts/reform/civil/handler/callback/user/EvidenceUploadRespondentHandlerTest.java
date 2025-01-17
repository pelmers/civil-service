package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert.EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert.JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial.AUTHORITIES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial.COSTS;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial.DOCUMENTARY;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness.DOCUMENTS_REFERRED;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness.WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness.WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadRespondentHandler.class,
    JacksonAutoConfiguration.class
})
class EvidenceUploadRespondentHandlerTest extends BaseCallbackHandlerTest {

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";
    @Autowired
    private EvidenceUploadRespondentHandler handler;

    @MockBean
    private Time time;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final UploadEvidenceExpert uploadEvidenceDate = new UploadEvidenceExpert();
    private final UploadEvidenceWitness uploadEvidenceDate2 = new UploadEvidenceWitness();
    private final UploadEvidenceDocumentType uploadEvidenceDate3 = new UploadEvidenceDocumentType();

    private static final String PAGE_ID = "validateValuesRespondent";

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(LocalDateTime.now());
    }

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackUnSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .claimType(ClaimType.CLINICAL_NEGLIGENCE)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(5000))
                            .build())
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), any())).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseProgAllocatedTrack").isEqualTo("SMALL_CLAIM");
    }

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .claimType(null)
            .totalClaimAmount(BigDecimal.valueOf(12500))
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), any())).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseProgAllocatedTrack").isEqualTo("FAST_CLAIM");
    }

    @Test
    void givenAboutToStart_1v2SameSolicitorWillNotChangeToRespondentTwoFlag() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(YES)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isNotEqualTo("RespondentTwoFields");
    }

    @Test
    void givenAboutToStart_1v2DifferentSolicitorsWillChangeToRespondentTwoFlag() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isEqualTo("RespondentTwoFields");
    }

    @Test
    void givenAboutToStart_1v2DifferentSolicitorsWillChangeToRespondentTwoFlagSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YES)
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isEqualTo("RespondentTwoFields");
    }

    @Test
    void givenAboutToStart_1v1WillNotChangeToRespondentTwoFlag() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isNotEqualTo("RespondentTwoFields");
    }

    static Stream<Arguments> witnessOptionsSelected() {
        List<EvidenceUploadWitness> witnessList = new ArrayList<>();
        witnessList.add(WITNESS_STATEMENT);
        witnessList.add(WITNESS_SUMMARY);
        witnessList.add(DOCUMENTS_REFERRED);
        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                          .witnessSelectionEvidenceRes(witnessList).build()),
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                          .witnessSelectionEvidenceSmallClaimRes(witnessList).build())
        );
    }

    static Stream<Arguments> expertOptionsSelected() {
        List<EvidenceUploadExpert> expertList = new ArrayList<>();
        expertList.add(EXPERT_REPORT);
        expertList.add(JOINT_STATEMENT);
        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                          .expertSelectionEvidenceRes(expertList).build()),
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                          .expertSelectionEvidenceSmallClaimRes(expertList).build())
        );
    }

    static Stream<Arguments> trialOptionsSelected() {
        List<EvidenceUploadTrial> trialList = new ArrayList<>();
        trialList.add(AUTHORITIES);
        trialList.add(DOCUMENTARY);
        trialList.add(COSTS);
        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                          .trialSelectionEvidenceRes(trialList).build()),
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                          .trialSelectionEvidenceSmallClaimRes(trialList).build())
        );
    }

    static Stream<Arguments> optionsNotSelected() {

        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                          .build())
        );
    }

    @ParameterizedTest
    @MethodSource("witnessOptionsSelected")
    void shouldSetWitnessFlag_whenWitnessOptionsAreSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("witnessStatementFlag").isEqualTo("show_witness_statement");
        assertThat(response.getData()).extracting("witnessSummaryFlag").isEqualTo("show_witness_summary");
        assertThat(response.getData()).extracting("witnessReferredStatementFlag").isEqualTo("show_witness_referred");
    }

    @ParameterizedTest
    @MethodSource("optionsNotSelected")
    void shouldNotSetWitnessFlag_whenWitnessOptionsAreNotSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("witnessStatementFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("witnessSummaryFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("witnessReferredStatementFlag").isEqualTo("do_not_show");
    }

    @ParameterizedTest
    @MethodSource("expertOptionsSelected")
    void shouldSetExpertFlag_whenExpertOptionsAreSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("expertReportFlag").isEqualTo("show_expert_report");
        assertThat(response.getData()).extracting("expertJointFlag").isEqualTo("show_joint_expert");
    }

    @ParameterizedTest
    @MethodSource("optionsNotSelected")
    void shouldNotSetExpertFlag_whenExpertOptionsAreNotSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("expertReportFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("expertJointFlag").isEqualTo("do_not_show");
    }

    @ParameterizedTest
    @MethodSource("trialOptionsSelected")
    void shouldSetTrialFlag_whenTrialOptionsAreSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("trialAuthorityFlag").isEqualTo("show_trial_authority");
        assertThat(response.getData()).extracting("trialDocumentaryFlag").isEqualTo("show_trial_documentary");
        assertThat(response.getData()).extracting("trialCostsFlag").isEqualTo("show_trial_costs");
    }

    @ParameterizedTest
    @MethodSource("optionsNotSelected")
    void shouldNotSetTrialFlag_whenTrialOptionsAreNotSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("trialAuthorityFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("trialDocumentaryFlag").isEqualTo("do_not_show");
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes",
        "documentIssuedDate,documentReferredInStatementRes",
        "documentIssuedDate,documentEvidenceForTrialRes",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes",
        "documentIssuedDate,documentReferredInStatementRes",
        "documentIssuedDate,documentEvidenceForTrialRes",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentForDisclosureRes, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentReferredInStatementRes, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (4).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureOneRespondent(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes",
        "expertOptionUploadDate,documentJointStatementRes",
        "expertOptionUploadDate,documentQuestionsRes",
        "expertOptionUploadDate,documentAnswersRes",
    })
    void shouldNotReturnError_whenExpert2OptionUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes",
        "expertOptionUploadDate,documentJointStatementRes",
        "expertOptionUploadDate,documentQuestionsRes",
        "expertOptionUploadDate,documentAnswersRes",
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (5).",
        "expertOptionUploadDate,documentJointStatementRes,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (6).",
        "expertOptionUploadDate,documentQuestionsRes,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (7).",
        "expertOptionUploadDate,documentAnswersRes,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (8).",
    })
    void shouldReturnError_whenExpertOptionUploadDateFutureOneRespondent(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (3).",
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFutureOneRespondent(String dateField, String collectionField,
                                                               String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes",
        "witnessOptionUploadDate,documentHearsayNoticeRes",
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes",
        "witnessOptionUploadDate,documentHearsayNoticeRes",
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (3).",
        "witnessOptionUploadDate,documentWitnessStatementRes,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (3)."
    })
    void shouldReturnError_whenOneDateIsInFutureForWitnessStatementsOneRespondent(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.now().toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes2",
        "documentIssuedDate,documentReferredInStatementRes2",
        "documentIssuedDate,documentEvidenceForTrialRes2",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePastTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes2",
        "documentIssuedDate,documentReferredInStatementRes2",
        "documentIssuedDate,documentEvidenceForTrialRes2",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresentTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes2, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentReferredInStatementRes2, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (4).",
        "documentIssuedDate,documentEvidenceForTrialRes2, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (9).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureTwoRespondent(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2",
        "expertOptionUploadDate,documentJointStatementRes2",
        "expertOptionUploadDate,documentQuestionsRes2",
        "expertOptionUploadDate,documentAnswersRes2"
    })
    void shouldNotReturnError_whenExpert2OptionUploadDatePastTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2",
        "expertOptionUploadDate,documentJointStatementRes2",
        "expertOptionUploadDate,documentQuestionsRes2",
        "expertOptionUploadDate,documentAnswersRes2"
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresentTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (5).",
        "expertOptionUploadDate,documentJointStatementRes2,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (6).",
        "expertOptionUploadDate,documentQuestionsRes2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (7).",
        "expertOptionUploadDate,documentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (8)."
    })
    void shouldReturnError_whenExpertOptionUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes2,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes2,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (3).",
        "witnessOptionUploadDate,documentWitnessStatementRes2,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes2,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (3)."
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFuture(String dateField, String collectionField,
                                                               String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2",
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2",
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (5).",
        "expertOptionUploadDate,documentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (8).",
        "expertOptionUploadDate,documentQuestionsRes2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (7).",
        "expertOptionUploadDate,documentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (8)."
    })
    void shouldReturnError_whenOneDateIsInFutureForExpertStatementsTwoRespondents(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now().toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @Test
    void shouldCallExternalTask_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then
        assertThat(updatedData.getCaseDocumentUploadDateRes()).isEqualTo(time.now());
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsTwoRespondentSpec() {
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        var documentUpload = UploadEvidenceDocumentType.builder().documentUpload(testDocument).build();
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceDocumentType>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .documentForDisclosureRes2(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentForDisclosureRes2().get(0).getValue().getDocumentUpload()
                       .getCategoryID()).isEqualTo("RespondentTwoDisclosure");
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsTwoRespondentUnSpec() {
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        var documentUpload = UploadEvidenceDocumentType.builder().documentUpload(testDocument).build();
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceDocumentType>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .documentForDisclosureRes2(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentForDisclosureRes2().get(0).getValue().getDocumentUpload()
                       .getCategoryID()).isEqualTo("RespondentTwoDisclosure");
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsOneRespondentSpec() {
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        var documentUpload = UploadEvidenceExpert.builder().expertDocument(testDocument).build();
        List<Element<UploadEvidenceExpert>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceExpert>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .documentAnswersRes(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentAnswersRes().get(0).getValue().getExpertDocument()
                       .getCategoryID()).isEqualTo("RespondentOneExpertAnswers");
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsOneRespondentUnSpec() {
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        var documentUpload = UploadEvidenceExpert.builder().expertDocument(testDocument).build();
        List<Element<UploadEvidenceExpert>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceExpert>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .documentQuestionsRes(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentQuestionsRes().get(0).getValue().getExpertDocument()
                       .getCategoryID()).isEqualTo("RespondentOneExpertQuestions");
    }

    @Test
    void shouldNotAssignCategoryID_whenDocumentNotExistsTwoRespondent() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentForDisclosureRes2()).isNull();
    }

    @Test
    void shouldNotAssignCategoryID_whenDocumentNotExistsOneRespondent() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentAnswersRes()).isNull();
    }

    @Test
    void givenSubmittedThenReturnsSubmittedCallbackResponse() {
        // Given
        String header = "# Documents uploaded";
        String body = "You can continue uploading documents or return later. To upload more documents, "
            + "go to Next step and select \"Document Upload\".";
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        // When
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        // Then
        assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                      .confirmationHeader(header)
                                                                      .confirmationBody(body)
                                                                      .build());
    }

    @Test
    void whenRegisterCalledThenReturnEvidenceUploadCaseEvent() {
        // Given
        Map<String, CallbackHandler> registerTarget = new HashMap<>();

        // When
        handler.register(registerTarget);

        // Then
        assertThat(registerTarget).containsExactly(entry(EVIDENCE_UPLOAD_RESPONDENT.name(), handler));
    }

    private <T, A> T invoke(T target, String method, A argument) {
        ReflectionUtils.invokeMethod(ReflectionUtils.getRequiredMethod(target.getClass(),
                                                                       method, argument.getClass()), target, argument);
        return target;
    }

    @Test
    void shouldAddRespondentEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded() {
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentQuestionsRes2(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummaryRes2(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentDisclosureListRes(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentDisclosureListRes2(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 10, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then respondent docs uploaded after bundle should return size 4
        assertThat(updatedData.getRespondentDocsUploadedAfterBundle().size()).isEqualTo(6);
    }

    @Test
    void shouldNotAddRespondentEvidenceDocWhenBundleCreatedDateIsAfterEvidenceUploaded() {
        // Given caseBundles with bundle created date is after witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 15, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then respondent docs uploaded after bundle should return size 0
        assertThat(updatedData.getRespondentDocsUploadedAfterBundle().size()).isEqualTo(0);
    }

    @Test
    void shouldBreakWhenThereIsAnyCaseBundlesWithoutCreatedDate() {
        // Given: No caseBundles exists with CreatedDate and new evidence is uploaded
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(LocalDateTime.of(2022, 05, 15, 12, 12, 12)))
            .build()));
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .build()));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(caseBundles)
            .build();

        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: handler is called
        // Then: an exception is thrown
        assertThrows(NullPointerException.class, () -> {
            handler.handle(params);
        });
    }

    @Test
    void shouldBreakWhenThereIsAnyCaseBundlesWithNullCreatedDate() {
        // Given: No caseBundles exists with CreatedDate and new evidence is uploaded
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.ofNullable(null))
            .build()));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(caseBundles)
            .build();

        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: handler is called
        // Then: an exception is thrown
        assertThrows(NullPointerException.class, () -> {
            handler.handle(params);
        });
    }

    private List<IdValue<Bundle>> prepareCaseBundles(LocalDateTime bundleCreatedDate) {
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(bundleCreatedDate))
            .build()));
        return caseBundles;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs(LocalDateTime uploadedDate) {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 TEST_URL)
                                                                                    .documentFileName(TEST_FILE_NAME).build())
                                                         .witnessOptionName("FirstName LastName")
                                                         .createdDatetime(uploadedDate)
                                                         .witnessOptionUploadDate(LocalDate.of(2023, 2, 10)).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs(LocalDateTime uploadedDate) {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .createdDatetime(uploadedDate)
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build()).build()));

        return  expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> getUploadEvidenceDocumentTypeDocs(LocalDateTime uploadedDate) {
        List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocs = new ArrayList<>();
        uploadEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                        .builder()
                                                        .createdDatetime(uploadedDate)
                                                        .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build()).build()));

        return  uploadEvidenceDocs;
    }
}

