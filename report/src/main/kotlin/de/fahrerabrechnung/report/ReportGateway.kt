package de.fahrerabrechnung.report

/** Placeholder only. PDF generation starts only after the mandatory 0.4.1 checkpoint is reviewed. */
interface ReportGateway {
    suspend fun readiness(): ReportReadiness
}

enum class ReportReadiness { NOT_IMPLEMENTED }

class NoOpReportGateway : ReportGateway {
    override suspend fun readiness() = ReportReadiness.NOT_IMPLEMENTED
}
