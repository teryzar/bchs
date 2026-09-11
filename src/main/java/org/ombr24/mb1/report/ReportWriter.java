package org.ombr24.mb1.report;

import org.ombr24.mb1.utils.EngagementContext;

public interface ReportWriter {
    void write(String outputFile, EngagementContext context);
}
