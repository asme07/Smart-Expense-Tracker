package com.example.expensetracker.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.expensetracker.models.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility for exporting transactions to a CSV file and sharing via Intent.
 */
public class CsvExportUtils {

    public static void exportTransactionsToCsv(Context context, List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            Toast.makeText(context, "No transactions available to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File exportDir = new File(context.getCacheDir(), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String fileName = "Expense_Report_" + System.currentTimeMillis() + ".csv";
            File file = new File(exportDir, fileName);

            FileWriter writer = new FileWriter(file);
            // Write CSV Header
            writer.append("Transaction ID,Date,Type,Category,Amount,Description\n");

            for (Transaction t : transactions) {
                writer.append("\"").append(t.getTransactionId()).append("\",");
                writer.append("\"").append(t.getDate()).append("\",");
                writer.append("\"").append(t.getType()).append("\",");
                writer.append("\"").append(t.getCategory()).append("\",");
                writer.append(String.valueOf(t.getAmount())).append(",");
                String desc = t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : "";
                writer.append("\"").append(desc).append("\"\n");
            }

            writer.flush();
            writer.close();

            // Share via FileProvider
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Smart Expense App - Transaction History");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "Share CSV Report"));

        } catch (IOException e) {
            Toast.makeText(context, "Failed to export CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
