package com.neonforge.launcher.density

import com.neonforge.launcher.privilege.PrivilegeRouter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SessionPhase { IDLE, APPLYING, IN_SESSION, RESTORING, ERROR }

data class SessionSnapshot(
    val phase: SessionPhase = SessionPhase.IDLE,
    val message: String = "",
    val currentDensity: Int? = null,
    val physicalDensity: Int? = null
)

/**
 * Density engine state machine.
 * Uses PrivilegeRouter for all command execution (Termux-ADB primary, Shizuku fallback).
 * Every APPLY and RESTORE is followed by a verification read.
 */
class DensityController(
    private val router: PrivilegeRouter,
    private val journalRepo: RecoveryJournalRepo,
    private val opLog: OperationLog
) {
    private val _session = MutableStateFlow(SessionSnapshot())
    val session: StateFlow<SessionSnapshot> = _session.asStateFlow()

    suspend fun readCurrent(): Result<DensityState> {
        val res = router.runCommand("wm", "density")
        if (res.exitCode != 0) {
            opLog.record("READ", res.exitCode, res.stderr.ifBlank { res.stdout }, false)
            _session.value = _session.value.copy(
                message = "Read failed: ${res.stderr.ifBlank { res.stdout }}"
            )
            return Result.failure(IllegalStateException("wm density read failed (exit ${res.exitCode})"))
        }
        val state = WmDensityParser.parse(res.stdout)
            ?: return Result.failure(IllegalStateException("Unparseable wm density output"))
        opLog.record("READ", 0, "physical=${state.physical} override=${state.override}", true)
        _session.value = _session.value.copy(
            currentDensity = state.effective,
            physicalDensity = state.physical
        )
        return Result.success(state)
    }

    suspend fun startSession(gamePackage: String, gameLabel: String, target: Int): Result<Unit> {
        _session.value = SessionSnapshot(phase = SessionPhase.APPLYING)
        val current = readCurrent().getOrElse {
            _session.value = SessionSnapshot(
                phase = SessionPhase.ERROR,
                message = it.message ?: "baseline read failed"
            )
            return Result.failure(it)
        }
        journalRepo.begin(
            RecoveryJournal(
                sessionActive = true,
                gamePackage = gamePackage,
                gameLabel = gameLabel,
                physicalDensity = current.physical,
                baselineOverride = current.override,
                targetDensity = target,
                startedAt = System.currentTimeMillis()
            )
        )
        val apply = router.runCommand("wm", "density", target.toString())
        if (apply.exitCode != 0) {
            opLog.record("APPLY", apply.exitCode, apply.stderr.ifBlank { apply.stdout }, false)
            _session.value = SessionSnapshot(
                phase = SessionPhase.ERROR,
                message = "Apply failed: ${apply.stderr.ifBlank { apply.stdout }}"
            )
            return Result.failure(IllegalStateException("apply failed"))
        }
        val verify = readCurrent().getOrElse {
            _session.value = SessionSnapshot(
                phase = SessionPhase.ERROR,
                message = "Post-apply verification read failed"
            )
            return Result.failure(it)
        }
        if (verify.effective != target) {
            opLog.record("APPLY_VERIFY", 0, "expected=$target actual=${verify.effective}", false)
            _session.value = SessionSnapshot(
                phase = SessionPhase.ERROR,
                message = "Verification mismatch: expected $target, got ${verify.effective}"
            )
            return Result.failure(IllegalStateException("verification mismatch"))
        }
        opLog.record("APPLY", 0, "target=$target verified", true)
        _session.value = SessionSnapshot(
            phase = SessionPhase.IN_SESSION,
            message = "Density $target active for $gameLabel",
            currentDensity = verify.effective,
            physicalDensity = verify.physical
        )
        return Result.success(Unit)
    }

    suspend fun restoreSession(journal: RecoveryJournal): Result<Unit> {
        _session.value = _session.value.copy(phase = SessionPhase.RESTORING)
        val command = if (journal.baselineOverride == null) {
            arrayOf("wm", "density", "reset")
        } else {
            arrayOf("wm", "density", journal.baselineOverride.toString())
        }
        val restore = router.runCommand(*command)
        if (restore.exitCode != 0) {
            opLog.record("RESTORE", restore.exitCode, restore.stderr.ifBlank { restore.stdout }, false)
            _session.value = SessionSnapshot(
                phase = SessionPhase.ERROR,
                message = "Restore failed: ${restore.stderr.ifBlank { restore.stdout }}"
            )
            return Result.failure(IllegalStateException("restore failed"))
        }
        val verify = readCurrent().getOrElse {
            _session.value = SessionSnapshot(
                phase = SessionPhase.ERROR,
                message = "Post-restore verification read failed"
            )
            return Result.failure(it)
        }
        val expected = journal.baselineOverride ?: journal.physicalDensity
        if (verify.effective != expected) {
            opLog.record("RESTORE_VERIFY", 0, "expected=$expected actual=${verify.effective}", false)
            _session.value = SessionSnapshot(
                phase = SessionPhase.ERROR,
                message = "Restore verification mismatch: expected $expected, got ${verify.effective}"
            )
            return Result.failure(IllegalStateException("restore verification mismatch"))
        }
        opLog.record("RESTORE", 0, "baseline restored effective=$expected", true)
        journalRepo.clear()
        _session.value = SessionSnapshot(
            phase = SessionPhase.IDLE,
            message = "Baseline restored and verified",
            currentDensity = verify.effective,
            physicalDensity = verify.physical
        )
        return Result.success(Unit)
    }
}
