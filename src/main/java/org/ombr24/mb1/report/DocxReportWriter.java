package org.ombr24.mb1.report;

import org.apache.poi.xwpf.usermodel.*;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.utils.EngagementContext;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DocxReportWriter implements ReportWriter {

    private final String templatePath;

    public DocxReportWriter(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public void write(String outputFile, EngagementContext context) {

        try (XWPFDocument doc =
                     new XWPFDocument(new FileInputStream(templatePath))) {

            Map<String, String> placeholders =
                    buildHeaderPlaceholders(context);

            replaceText(doc, placeholders);
            fillEngagementTable(doc, context);

            try (FileOutputStream out =
                         new FileOutputStream(outputFile)) {
                doc.write(out);
            }

        } catch (Exception e) {
            throw new IllegalStateException("DOCX generation failed", e);
        }
    }

    /* =========================
       Header placeholders
       ========================= */
    private Map<String, String> buildHeaderPlaceholders(
            EngagementContext context) {
//
//        return Map.of(
//                "{{date}}", LocalDate.now().toString(),
//                "{{total}}", String.valueOf(
//                        context.getAssignments()
//                                .values()
//                                .stream()
//                                .mapToInt(List::size)
//                                .sum()
//                )
//        );
        return null;
    }

    /* =========================
       Text replacement
       ========================= */
    private void replaceText(XWPFDocument doc,
                             Map<String, String> data) {

        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, data);
        }

        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraph(p, data);
                    }
                }
            }
        }
    }

    private void replaceInParagraph(XWPFParagraph p,
                                    Map<String, String> data) {

        for (XWPFRun run : p.getRuns()) {
            String text = run.getText(0);
            if (text == null) continue;

            for (Map.Entry<String, String> e : data.entrySet()) {
                if (text.contains(e.getKey())) {
                    text = text.replace(e.getKey(), e.getValue());
                }
            }
            run.setText(text, 0);
        }
    }

    /* =========================
       Table filling
       ========================= */
    private void fillEngagementTable(XWPFDocument doc,
                                     EngagementContext context) {

        // очікуємо ПЕРШУ таблицю в шаблоні
        XWPFTable table = doc.getTables().get(0);

        // рядок 0 — заголовок
        // рядок 1 — шаблон {{name}} | {{rank}} | {{position}}
        XWPFTableRow templateRow = table.getRow(1);

        table.removeRow(1);

        for (Map.Entry<String, List<BattleUnit>> e :
                context.getAssignments().entrySet()) {

            String position = e.getKey();

            for (BattleUnit unit : e.getValue()) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(unit.getName());
                row.getCell(1).setText(unit.getRank());
                row.getCell(2).setText(position);
            }
        }
    }
}
