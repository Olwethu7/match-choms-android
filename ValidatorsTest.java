package com.match.choms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.match.choms.model.Beneficiary;
import com.match.choms.model.Referral;
import com.match.choms.model.Visit;
import com.match.choms.util.DateUtils;
import com.match.choms.util.Validators;

import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

/** Unit tests for the form validation rules (pure Java - no Android device needed). */
public class ValidatorsTest {

    private Beneficiary validBeneficiary() {
        Beneficiary b = new Beneficiary();
        b.firstName = "Thandi"; b.lastName = "Nkosi"; b.idNumber = "7806155123089";
        b.dob = ""; b.gender = ""; b.phone = "0821234567"; b.area = "Umlazi";
        b.programme = "Maternal Health"; b.enrolmentDate = DateUtils.todayISO();
        b.assignedChwId = "chw1"; b.consent = true;
        return b;
    }

    @Test public void luhnChecksum() {
        assertTrue(Validators.luhnValid("1503120123080"));
        assertFalse(Validators.luhnValid("1503120123081"));
    }

    @Test public void saIdGivesDobAndGender() {
        Validators.SaId id = Validators.parseSaId("7806155123089");
        assertTrue(id.valid);
        assertEquals("1978-06-15", id.dob);
        assertEquals("male", id.gender);
        assertFalse(Validators.parseSaId("123").valid);
    }

    @Test public void phoneIsNormalised() {
        assertEquals("+27821234567", Validators.normalisePhone("082 123 4567"));
        assertNull(Validators.normalisePhone("12345"));
    }

    @Test public void loginRequiresEmailAndPassword() {
        assertEquals(2, Validators.validateLogin("", "").size());
        assertTrue(Validators.validateLogin("a@b.co", "x").isEmpty());
    }

    @Test public void validBeneficiaryPassesAndIsCleaned() {
        Beneficiary b = validBeneficiary();
        assertTrue(Validators.validateBeneficiary(b, null).isEmpty());
        assertEquals("1978-06-15", b.dob);
        assertEquals("+27821234567", b.phone);
    }

    @Test public void consentIsRequired() {
        Beneficiary b = validBeneficiary();
        b.consent = false;
        assertTrue(Validators.validateBeneficiary(b, null).containsKey("consent"));
    }

    @Test public void duplicateIdIsRejected() {
        Beneficiary other = new Beneficiary();
        other.id = "x"; other.idNumber = "7806155123089"; other.firstName = "A"; other.lastName = "B";
        Map<String, String> e = Validators.validateBeneficiary(validBeneficiary(), Arrays.asList(other));
        assertTrue(e.containsKey("idNumber"));
    }

    @Test public void completedVisitNeedsNotesAndOutcome() {
        Visit v = new Visit();
        v.beneficiaryId = "b"; v.chwId = "c"; v.status = "completed"; v.date = DateUtils.todayISO();
        v.location = "Home";
        Map<String, String> e = Validators.validateVisit(v, true);
        assertTrue(e.containsKey("outcome"));
        assertTrue(e.containsKey("notes"));
    }

    @Test public void referralCompletedNeedsOutcome() {
        Referral r = new Referral();
        r.beneficiaryId = "b"; r.chwId = "c"; r.facility = "Clinic A"; r.reason = "Needs check-up";
        r.referralDate = DateUtils.todayISO(); r.status = "completed";
        assertTrue(Validators.validateReferral(r).containsKey("outcomeNotes"));
    }

    @Test public void impossibleDateRejected() {
        assertFalse(DateUtils.isValidISODate("2026-02-30"));
    }
}
