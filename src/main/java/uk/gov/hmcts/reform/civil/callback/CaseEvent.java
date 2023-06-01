package uk.gov.hmcts.reform.civil.callback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uk.gov.hmcts.reform.civil.callback.UserType.CAMUNDA;
import static uk.gov.hmcts.reform.civil.callback.UserType.TESTING_SUPPORT;
import static uk.gov.hmcts.reform.civil.callback.UserType.USER;

@Getter
@RequiredArgsConstructor
public enum CaseEvent {
    CREATE_CLAIM(USER),
    CREATE_CLAIM_SPEC(USER),
    CREATE_SERVICE_REQUEST_CLAIM(USER),
    CREATE_CLAIM_SPEC_AFTER_PAYMENT(USER),
    CREATE_CLAIM_AFTER_PAYMENT(USER),
    CREATE_LIP_CLAIM(USER),
    ENTER_BREATHING_SPACE_SPEC(USER),
    LIFT_BREATHING_SPACE_SPEC(USER),
    NOTIFY_DEFENDANT_OF_CLAIM(USER),
    NOTIFY_DEFENDANT_OF_CLAIM_DETAILS(USER),
    ADD_OR_AMEND_CLAIM_DOCUMENTS(USER),
    ACKNOWLEDGE_CLAIM(USER),
    ACKNOWLEDGEMENT_OF_SERVICE(USER),
    ADD_DEFENDANT_LITIGATION_FRIEND(USER),
    INFORM_AGREED_EXTENSION_DATE(USER),
    INFORM_AGREED_EXTENSION_DATE_SPEC(USER),
    MEDIATION_SUCCESSFUL(USER),
    MEDIATION_UNSUCCESSFUL(USER),

    DEFENDANT_RESPONSE(USER),
    DEFENDANT_RESPONSE_SPEC(USER),
    DEFENDANT_RESPONSE_CUI(USER),
    UPLOAD_TRANSLATED_DOCUMENT(USER),
    CLAIMANT_RESPONSE(USER),
    CLAIMANT_RESPONSE_SPEC(USER),
    WITHDRAW_CLAIM(USER),
    DISCONTINUE_CLAIM(USER),
    DISMISS_CLAIM(USER),
    CREATE_CASE_FLAGS(USER),
    MANAGE_CASE_FLAGS(USER),

    HEARING_FEE_UNPAID(USER),
    HEARING_FEE_PAID(USER),
    MAIN_CASE_CLOSED(USER),
    APPLICATION_PROCEEDS_IN_HERITAGE(USER),
    CASE_PROCEEDS_IN_CASEMAN(USER),
    RESUBMIT_CLAIM(USER),
    AMEND_PARTY_DETAILS(USER),
    CHANGE_SOLICITOR_EMAIL(USER),
    TAKE_CASE_OFFLINE(USER),
    TRIAL_READY_CHECK(USER),
    TRIAL_READY_NOTIFICATION(USER),
    MOVE_TO_DECISION_OUTCOME(USER),
    ADD_CASE_NOTE(USER),
    DEFAULT_JUDGEMENT_SPEC(USER),
    DEFAULT_JUDGEMENT(USER),
    REQUEST_JUDGEMENT_ADMISSION_SPEC(USER),
    INITIATE_GENERAL_APPLICATION(USER),
    CREATE_SDO(USER),
    REFER_TO_JUDGE(USER),
    STANDARD_DIRECTION_ORDER_DJ(USER),
    HEARING_SCHEDULED(USER),
    NotSuitable_SDO(USER),
    EVIDENCE_UPLOAD_JUDGE(USER),
    SERVICE_REQUEST_RECEIVED(USER),
    NOC_REQUEST(USER),
    APPLY_NOC_DECISION(USER),
    BUNDLE_CREATION_CHECK(USER),
    RESET_PIN(USER),
    GENERATE_DIRECTIONS_ORDER(USER),
    EVIDENCE_UPLOAD_APPLICANT(USER),
    EVIDENCE_UPLOAD_RESPONDENT(USER),
    EVIDENCE_UPLOAD_CHECK(USER),
    CREATE_BUNDLE(USER),
    EXTEND_RESPONSE_DEADLINE(USER),

    APPLICANT_TRIAL_READY_NOTIFY_OTHERS(USER),
    RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS(USER),
    RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS(USER),
    GENERATE_TRIAL_READY_DOCUMENT_APPLICANT(USER),
    GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1(USER),
    GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT2(USER),
    NOTIFY_FORMER_SOLICITOR(CAMUNDA),
    NOTIFY_OTHER_SOLICITOR_1(CAMUNDA),
    NOTIFY_OTHER_SOLICITOR_2(CAMUNDA),
    TRIAL_READINESS(USER),
    BUNDLE_CREATION_NOTIFICATION(USER),
    ASSIGN_CASE_TO_APPLICANT_SOLICITOR1(CAMUNDA),
    ASSIGN_CASE_TO_APPLICANT_SOLICITOR1_SPEC(CAMUNDA),
    TRIGGER_APPLICATION_CLOSURE(CAMUNDA),
    APPLICATION_CLOSED_UPDATE_CLAIM(CAMUNDA),
    TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE(CAMUNDA),
    APPLICATION_OFFLINE_UPDATE_CLAIM(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_REQUEST_FOR_EXTENSION(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_EXTENSION_RESPONSE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_TRIAL_READY(CAMUNDA),
    GENERATE_ORDER_NOTIFICATION(CAMUNDA),

    NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY(CAMUNDA),

    NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT_SPEC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC(CAMUNDA),
    NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_LIP_DEFENDANT_RESPONSE_SUBMISSION(CAMUNDA),
    NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED(CAMUNDA),
    NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_LITIGATION_FRIEND_ADDED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_LITIGATION_FRIEND_ADDED(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_LITIGATION_FRIEND_ADDED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED(CAMUNDA),
    NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED(CAMUNDA),
    NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT(CAMUNDA),
    NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT(CAMUNDA),
    NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT(CAMUNDA),
    NOTIFY_APPLICANT_MEDIATION_AGREEMENT(CAMUNDA),
    NOTIFY_RESPONDENT_MEDIATION_AGREEMENT(CAMUNDA),
    NOTIFY_INTERIM_JUDGMENT_CLAIMANT(CAMUNDA),
    NOTIFY_INTERIM_JUDGMENT_DEFENDANT(CAMUNDA),
    NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION(CAMUNDA),
    NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION(CAMUNDA),
    NOTIFY_DIRECTION_ORDER_DJ_CLAIMANT(CAMUNDA),
    NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CONTACT_DETAILS_CHANGE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CUI(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_HEARING_FEE_UNPAID(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_HEARING_FEE_UNPAID(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC(CAMUNDA),

    DISPATCH_BUSINESS_PROCESS(CAMUNDA),
    START_BUSINESS_PROCESS(CAMUNDA),
    START_BUSINESS_PROCESS_GASPEC(CAMUNDA),
    END_BUSINESS_PROCESS(CAMUNDA),

    MAKE_PBA_PAYMENT(CAMUNDA),
    MAKE_PBA_PAYMENT_SPEC(CAMUNDA),
    VALIDATE_FEE(CAMUNDA),
    VALIDATE_FEE_SPEC(CAMUNDA),

    CREATE_SERVICE_REQUEST_API(CAMUNDA),
    MAKE_SERVICE_REQUEST_PBA_PAYMENT(CAMUNDA),

    GENERATE_CLAIM_FORM(CAMUNDA),
    GENERATE_CLAIM_FORM_SPEC(CAMUNDA),

    PROCESS_FULL_DEFENCE(CAMUNDA),
    PROCESS_FULL_DEFENCE_SPEC(CAMUNDA),
    PROCESS_PART_ADMISSION_DEFENCE_SPEC(CAMUNDA),
    PROCESS_CLAIM_ISSUE(CAMUNDA),
    PROCESS_CLAIM_ISSUE_SPEC(CAMUNDA),
    PROCESS_PAYMENT_FAILED(CAMUNDA),

    PROCEEDS_IN_HERITAGE_SYSTEM(CAMUNDA),
    PROCEEDS_IN_HERITAGE_SYSTEM_SPEC(CAMUNDA),
    GENERATE_ACKNOWLEDGEMENT_OF_CLAIM(CAMUNDA),
    GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC(CAMUNDA),
    GENERATE_DIRECTIONS_QUESTIONNAIRE(CAMUNDA),
    GENERATE_RESPONSE_SEALED(CAMUNDA),
    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_RPA_ON_CONTINUOUS_FEED(CAMUNDA),
    RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE(CAMUNDA),
    RESET_RPA_NOTIFICATION_BUSINESS_PROCESS(CAMUNDA),

    UPDATE_CASE_DETAILS_AFTER_NOC(CAMUNDA),

    UPDATE_CASE_DATA(TESTING_SUPPORT),

    GENERATE_DJ_FORM(CAMUNDA),
    GENERATE_DJ_FORM_SPEC(CAMUNDA),
    GENERATE_HEARING_FORM(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED(CAMUNDA),
    NOTIFY_CASEWORKER_DJ_RECEIVED(CAMUNDA),
    ADD_PDF_TO_MAIN_CASE(CAMUNDA),
    JUDICIAL_REFERRAL(CAMUNDA),
    migrateCase(CAMUNDA),
    NOTIFY_CLAIMANT_HEARING(CAMUNDA),
    NOTIFY_DEFENDANT1_HEARING(CAMUNDA),
    NOTIFY_DEFENDANT2_HEARING(CAMUNDA),
    NOTIFY_RPA_DJ_UNSPEC(CAMUNDA),
    NOTIFY_RPA_DJ_SPEC(CAMUNDA),
    GENERATE_TRIAL_READY_FORM_APPLICANT(CAMUNDA),
    GENERATE_TRIAL_READY_FORM_RESPONDENT1(CAMUNDA),
    GENERATE_TRIAL_READY_FORM_RESPONDENT2(CAMUNDA),
    TRIGGER_LOCATION_UPDATE(USER),
    TRIGGER_UPDATE_GA_LOCATION(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED(CAMUNDA);

    private final UserType userType;

    public boolean isCamundaEvent() {
        return this.getUserType() == CAMUNDA;
    }

}
