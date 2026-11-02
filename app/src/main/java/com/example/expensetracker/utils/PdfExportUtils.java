package com.example.expensetracker.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.expensetracker.models.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utility for generating professional PDF statement reports using Android's native PdfDocument.
 */
public class PdfExportUtils {

    public static void generateAndSharePdf(Context context, String userName, String monthTitle,
                                          double totalIncome, double totalExpense, List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            Toast.makeText(context, "No transactions to include in PDF statement.", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        // A4 page: 595 x 842 points
        int pageWidth = 595;
        int pageHeight = 842;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Header Background Banner (Dark Navy)
        paint.setColor(Color.parseColor("#0F172A"));
        canvas.drawRect(0, 0, pageWidth, 110, paint);

        // Header Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(20f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("SMART EXPENSE & BUDGET REPORT", 36, 45, paint);

        // Header Subtitle
        paint.setTextSize(11f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(Color.parseColor("#94A3B8"));
        canvas.drawText("Generated for: " + userName + " | Period: " + monthTitle, 36, 68, paint);
        canvas.drawText("Date Generated: " + DateUtils.getCurrentDate(), 36, 88, paint);

        // Summary Cards Section
        int cardTop = 130;
        int cardHeight = 70;
        int cardWidth = (pageWidth - 72 - 20) / 3;

        // Card 1: Income
        drawSummaryCard(canvas, 36, cardTop, cardWidth, cardHeight, "Total Income",
                CurrencyManager.formatAmount(context, totalIncome), Color.parseColor("#ECFDF5"), Color.parseColor("#10B981"));

        // Card 2: Expense
        drawSummaryCard(canvas, 36 + cardWidth + 10, cardTop, cardWidth, cardHeight, "Total Expense",
                CurrencyManager.formatAmount(context, totalExpense), Color.parseColor("#FEF2F2"), Color.parseColor("#EF4444"));

        // Card 3: Net Balance
        double net = totalIncome - totalExpense;
        drawSummaryCard(canvas, 36 + (cardWidth * 2) + 20, cardTop, cardWidth, cardHeight, "Net Savings",
                CurrencyManager.formatAmount(context, net), Color.parseColor("#EFF6FF"), Color.parseColor("#3B82F6"));

        // Table Header
        int tableTop = 230;
        paint.setColor(Color.parseColor("#F1F5F9"));
        canvas.drawRect(36, tableTop, pageWidth - 36, tableTop + 30, paint);

        paint.setColor(Color.parseColor("#0F172A"));
        paint.setTextSize(10f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DATE", 46, tableTop + 20, paint);
        canvas.drawText("TYPE", 130, tableTop + 20, paint);
        canvas.drawText("CATEGORY", 200, tableTop + 20, paint);
        canvas.drawText("DESCRIPTION", 310, tableTop + 20, paint);
        canvas.drawText("AMOUNT", pageWidth - 110, tableTop + 20, paint);

        // Table Rows
        int y = tableTop + 50;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        for (int i = 0; i < transactions.size(); i++) {
            // Check if page limit reached
            if (y > pageHeight - 50) {
                pdfDocument.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }

            Transaction t = transactions.get(i);

            // Row zebra striping
            if (i % 2 == 1) {
                paint.setColor(Color.parseColor("#F8FAFC"));
                canvas.drawRect(36, y - 16, pageWidth - 36, y + 10, paint);
            }

            paint.setColor(Color.parseColor("#334155"));
            paint.setTextSize(9f);
            canvas.drawText(t.getDate() != null ? t.getDate() : "", 46, y, paint);

            boolean isInc = t.isIncome();
            paint.setColor(isInc ? Color.parseColor("#059669") : Color.parseColor("#DC2626"));
            canvas.drawText(t.getType(), 130, y, paint);

            paint.setColor(Color.parseColor("#1E293B"));
            canvas.drawText(t.getCategory() != null ? t.getCategory() : "", 200, y, paint);

            String desc = t.getDescription() != null ? t.getDescription() : "-";
            if (desc.length() > 22) {
                desc = desc.substring(0, 19) + "...";
            }
            paint.setColor(Color.parseColor("#64748B"));
            canvas.drawText(desc, 310, y, paint);

            paint.setColor(isInc ? Color.parseColor("#059669") : Color.parseColor("#DC2626"));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            String amtStr = (isInc ? "+ " : "- ") + CurrencyManager.formatAmount(context, t.getAmount());
            canvas.drawText(amtStr, pageWidth - 110, y, paint);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

            // Line divider
            paint.setColor(Color.parseColor("#E2E8F0"));
            canvas.drawLine(36, y + 12, pageWidth - 36, y + 12, paint);

            y += 28;
        }

        // Footer on current page
        paint.setColor(Color.parseColor("#94A3B8"));
        paint.setTextSize(8f);
        canvas.drawText("Smart Expense & Budget Management App • Confidential Financial Record", 36, pageHeight - 20, paint);

        pdfDocument.finishPage(page);

        // Write to cache file
        try {
            File exportDir = new File(context.getCacheDir(), "statements");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            String fileName = "Expense_Statement_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(exportDir, fileName);
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            // Share via Intent
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Monthly Financial Statement - " + monthTitle);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Statement"));

        } catch (IOException e) {
            pdfDocument.close();
            Toast.makeText(context, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void drawSummaryCard(Canvas canvas, int x, int y, int width, int height,
                                       String label, String value, int bgColor, int textColor) {
        Paint p = new Paint();
        p.setAntiAlias(true);

        p.setColor(bgColor);
        canvas.drawRoundRect(x, y, x + width, y + height, 12, 12, p);

        p.setColor(Color.parseColor("#64748B"));
        p.setTextSize(9f);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText(label, x + 12, y + 24, p);

        p.setColor(textColor);
        p.setTextSize(13f);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(value, x + 12, y + 50, p);
    }
}
