# Smart Expense & Budget Management App 💳📊

A modern, production-grade Android application built with **Java and XML** in Android Studio, featuring comprehensive expense tracking, budget management, visual analytics via MPAndroidChart, offline-first SQLite persistence, and Firebase Cloud Firestore synchronization.

Developed as a **Final-Year B.Tech Computer Science and Engineering Capstone Project** and tailored for an **Android Developer Virtual Internship & Professional GitHub Portfolio**.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Problem Statement](#-problem-statement)
- [Objectives](#-objectives)
- [Core Features](#-core-features)
- [Technology Stack](#-technology-stack)
- [Application Architecture](#-application-architecture)
- [Database Design](#-database-design)
  - [SQLite Schema](#sqlite-schema-local-storage)
  - [Firestore Document Structure](#cloud-firestore-structure)
- [UI/UX Design System](#-uiux-design-system)
- [Screenshots & UI Previews](#-screenshots--ui-previews)
- [Installation & Setup](#-installation--setup)
- [Firebase Configuration Guide](#-firebase-configuration-guide)
- [Data Export (PDF & CSV)](#-data-export-pdf--csv)
- [Future Enhancements](#-future-enhancements)
- [Author & License](#-author--license)

---

## 🌟 Overview

**Smart Expense & Budget Management App** is an intuitive, fintech-inspired Android application designed to give users complete control over their personal finances. Unlike simple expense logs, this app combines:

1. **Dual Storage Architecture**: Reliable offline capability powered by local SQLite with seamless cloud backup via Firebase Cloud Firestore.
2. **Dynamic Financial Analytics**: Real-time spending visualization using MPAndroidChart (Category Pie Charts, Income vs. Expense Bar Charts, and Cumulative Daily Spending Trends).
3. **Budget Health Monitoring**: Category-wise and overall monthly budgets with automated threshold warnings (80% alert and over-budget notices).
4. **Statement Generation**: Native PDF financial statements and CSV data exports with Android share sheet integration.

---

## 🎯 Problem Statement

Modern individuals frequently struggle with personal budgeting, overspending across lifestyle categories, and lacking clear visibility into daily financial habits. Existing financial applications often suffer from:
- Cluttered, ad-heavy user interfaces.
- Hard dependency on internet connectivity for basic logging.
- Privacy concerns and vendor lock-in.
- Lack of exportable, audit-friendly financial statements.

**Smart Expense App** solves these challenges through an offline-first architecture, clean card-based UI, automated spending limits, and instant PDF report generation.

---

## 🎯 Objectives

- **Develop a 100% Native Java & XML Android Application** conforming strictly to Google's Material Design 3 guidelines.
- **Implement Robust Local Storage**: Use SQLite with optimized indexing and relational integrity for instant offline CRUD operations.
- **Provide Real-Time Cloud Sync**: Leverage Firebase Authentication and Firestore to secure and synchronize user finances across devices.
- **Deliver High-Impact Data Visualizations**: Render live interactive charts dynamically derived from actual transaction records.
- **Support Comprehensive Budgeting**: Provide visual alerts when monthly expenditures reach 80% or exceed limits.
- **Support Native Document Generation**: Create formatted monthly PDF financial summaries and CSV logs on-device without third-party web services.

---

## 🚀 Core Features

### 1. Authentication & Security
- **Firebase Email & Password Auth**: Secure user registration, authentication, and session persistence.
- **Password Visibility Toggle**: Material outlined text inputs with live form validation.
- **Password Reset**: Automated email recovery link delivery.
- **Demo / Offline Testing Mode**: One-tap guest login pre-seeded with realistic sample financial data for evaluation.
- **Protected User Isolation**: User data strictly scoped by unique `userId`.

### 2. Modern Fintech Dashboard
- **Total Net Balance Card**: Dynamic calculation of Income minus Expenses.
- **Privacy Eye Toggle**: Instantly hide/reveal account balances (`••••••`).
- **Monthly Income & Expense Indicators**: Upward emerald green and downward coral chips.
- **Overall Budget Progress Bar**: Color-coded progress with safe, warning, and overbudget statuses.
- **Quick Action Triggers**: Direct shortcuts for `+ Income`, `- Expense`, and Budget settings.
- **Recent Transactions List**: Custom RecyclerView highlighting the latest transactions.

### 3. Income & Expense Management (Full CRUD)
- **Fast Entry**: Segmented control to switch between Income and Expense.
- **Standardized Categories**:
  - *Income*: Salary, Freelancing, Business, Investments, Other.
  - *Expense*: Food, Shopping, Transportation, Education, Entertainment, Healthcare, Bills, Travel, Other.
- **Large Numerical Pad**: Currency symbol formatted input.
- **Interactive Calendar**: Date picker defaulting to current date.
- **Full Editing & Deletion**: Modifying existing records with confirmation dialogs.

### 4. Smart Budget Management
- **Overall Monthly Spending Limit**: Set total cap for the current calendar month.
- **Category-Wise Budgets**: Individual limits for high-spend categories (e.g., Food, Shopping, Bills).
- **Automated Threshold Warnings**:
  - 🟢 **Safe** (< 70% of limit)
  - 🟡 **Warning** (70% - 80% reached)
  - 🔴 **Danger / Exceeded** (> 80% or exceeded)
- **Visual Progress Trackers**: Custom multi-color progress bars.

### 5. Financial Analytics & Reports (MPAndroidChart)
- **Category Expense Breakdown (PieChart)**: Percentage slice distribution with center summary text.
- **Income vs. Expense (BarChart)**: Direct monthly cash flow comparison.
- **Daily Spending Trend (LineChart)**: Smooth bezier curve showing daily cumulative expense accumulation.
- **Summary Metrics**: Net Savings, Savings Rate %, and Top Spending Category.
- **Time Filtering**: Dropdown selector covering past months.

### 6. Transaction History & Search
- **Live Search**: Instant keyword filtering across descriptions and categories.
- **Filter Chips**: View All, Income Only, or Expense Only.
- **Category Filtering**: Dropdown filter for individual categories.
- **Multi-Criteria Sorting**: Newest First, Oldest First, Highest Amount, Lowest Amount.
- **Empty States**: Informative illustration and prompt when search yields no matches.

### 7. User Profile & Preferences
- **Multi-Currency Support**: Switch between **USD ($)**, **EUR (€)**, **GBP (£)**, **INR (₹)**, **JPY (¥)**, **CAD (C$)**, and **AUD (A$)**.
- **Dark & Light Mode**: Support for System Default, Force Light, or Force Dark modes via `AppCompatDelegate`.
- **Export to PDF**: Generates branded A4 PDF monthly statement with statistics table.
- **Export to CSV**: Generates standard comma-separated file shared via Android Intent.
- **Cloud Sync Now**: Manual on-demand two-way sync with Firebase Firestore.
- **Sign Out**: Secure session destruction.

---

## 🛠 Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Language** | Java | Strict Java 8/17 compatibility (No Kotlin) |
| **User Interface** | XML | Material Components, ConstraintLayout, CardView |
| **IDE** | Android Studio | Hedgehog / Iguana / Jellyfish compatible |
| **Authentication** | Firebase Auth | Email/Password provider & session tokens |
| **Cloud Database** | Firebase Cloud Firestore | NoSQL document storage (`users/{userId}/...`) |
| **Local Database** | SQLite | `SQLiteOpenHelper` with relational schema |
| **Charts & Graphs** | MPAndroidChart | `v3.1.0` (Pie, Bar, and Line charts) |
| **Build System** | Gradle | Android Gradle Plugin `8.2.2`, Gradle `8.4` |
| **Export Utilities** | Android `PdfDocument` | Native vector-rendered A4 PDF reports |
| **File Sharing** | AndroidX `FileProvider` | Secure scoped file URI sharing |

---

## 🏗 Application Architecture

The application strictly implements clean separation of concerns:

```
com.example.expensetracker/
├── activities/
│   ├── SplashActivity.java              # Launch screen with session router
│   ├── LoginActivity.java               # Authentication & Demo login
│   ├── RegisterActivity.java            # Account registration
│   ├── ForgotPasswordActivity.java      # Password recovery
│   ├── MainActivity.java                # Host for BottomNavigation & Fragments
│   ├── TransactionEditorActivity.java   # Add/Edit/Delete transaction
│   └── BudgetEditorActivity.java        # Add/Edit/Delete budget
├── fragments/
│   ├── HomeFragment.java                # Financial dashboard & recent items
│   ├── TransactionsFragment.java        # Searchable transaction history
│   ├── BudgetFragment.java              # Monthly & category budgets
│   ├── AnalyticsFragment.java           # MPAndroidChart reports
│   └── ProfileFragment.java             # Settings, Currency, Theme & Exports
├── adapters/
│   ├── TransactionAdapter.java          # Transaction list item binder
│   ├── BudgetAdapter.java               # Budget progress bar binder
│   └── CategoryAdapter.java             # Category selection grid binder
├── models/
│   ├── User.java                        # User profile entity
│   ├── Transaction.java                 # Financial transaction entity
│   ├── Budget.java                      # Spending limit entity
│   └── CategoryItem.java                # Category visual entity
├── database/
│   └── DatabaseHelper.java              # SQLiteOpenHelper & relational queries
├── firebase/
│   ├── FirebaseAuthManager.java         # Firebase Auth with local fallback
│   └── FirestoreManager.java            # Cloud Firestore two-way sync
└── utils/
    ├── CurrencyManager.java             # Multi-currency formatting
    ├── DateUtils.java                   # Date parsing and formatting
    ├── SessionManager.java              # SharedPreferences session store
    ├── CsvExportUtils.java              # CSV file generation & Intent share
    └── PdfExportUtils.java              # Native PDF statement generation
```

---

## 🗄 Database Design

### SQLite Schema (Local Storage)

#### 1. `users` Table
```sql
CREATE TABLE users (
    userId TEXT PRIMARY KEY,
    name TEXT,
    email TEXT,
    currency TEXT,
    createdAt INTEGER
);
```

#### 2. `transactions` Table
```sql
CREATE TABLE transactions (
    transactionId TEXT PRIMARY KEY,
    userId TEXT,
    amount REAL,
    type TEXT,           -- 'INCOME' or 'EXPENSE'
    category TEXT,       -- 'Food', 'Salary', etc.
    description TEXT,
    date TEXT,           -- 'YYYY-MM-DD'
    createdAt INTEGER
);
```

#### 3. `budgets` Table
```sql
CREATE TABLE budgets (
    budgetId TEXT PRIMARY KEY,
    userId TEXT,
    category TEXT,       -- 'All Categories' or specific category
    monthlyLimit REAL,
    month TEXT           -- 'YYYY-MM'
);
```

### Cloud Firestore Structure
```
users/
  └── {userId}/
        ├── name: "Alex Morgan"
        ├── email: "alex@example.com"
        ├── currency: "USD"
        ├── transactions/
        │     └── {transactionId}/
        │           ├── amount: 85.50
        │           ├── type: "EXPENSE"
        │           ├── category: "Food"
        │           ├── description: "Groceries"
        │           └── date: "2026-11-02"
        └── budgets/
              └── {budgetId}/
                    ├── category: "Food"
                    ├── monthlyLimit: 600.00
                    └── month: "2026-11"
```

---

## 🎨 UI/UX Design System

The visual design is inspired by leading fintech apps (Revolut, Mint, NuBank):
- **Color Palette**:
  - Primary Navy: `#0F172A` (Slate 900)
  - Surface Navy: `#1E293B` (Slate 800)
  - Income Accent: `#10B981` (Emerald Green)
  - Expense Accent: `#EF4444` (Rose / Coral Red)
  - Warning Accent: `#F59E0B` (Amber Orange)
  - Secondary Brand: `#3B82F6` (Electric Blue)
- **Cards & Elevation**: 16dp rounded corners with 1dp subtle stroke borders.
- **Typography**: Clean hierarchy with Sans-Serif Bold headers and muted metadata captions.
- **Theming**: Fully responsive light and dark mode implementations.

---

## 📸 Screenshots & UI Previews

| Splash Screen | Login & Auth | Dashboard (Light) | Dashboard (Dark) |
|:---:|:---:|:---:|:---:|
| Branded Launch | Form Validation | Gradient Balance Card | AMOLED Dark Theme |

| Transaction History | Budget Management | Financial Analytics | User Profile & Export |
|:---:|:---:|:---:|:---:|
| Search & Filters | Progress Bars & Alerts | MPAndroidChart Graphs | PDF/CSV & Theme |

---

## 📥 Installation & Setup

### Prerequisites
- **Android Studio** (Hedgehog 2023.1.1 or later recommended).
- **JDK 17 or JDK 21**.
- Android SDK with Platform 34 (Android 14) installed.

### Steps to Run
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/smart-expense-tracker.git
   cd smart-expense-tracker
   ```
2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select **Open** and choose the `SmartExpenseTracker` directory.
3. **Gradle Sync**:
   - Android Studio will automatically resolve dependencies:
     - AndroidX Material 1.11.0
     - MPAndroidChart v3.1.0
     - Firebase BOM 32.7.2
4. **Run the App**:
   - Connect an Android device with USB debugging enabled or launch an Android Virtual Device (AVD).
   - Press **Run** (`Shift + F10`).
5. **Instant Demo Mode**:
   - On the Login screen, click **"Try Demo / Quick Login"** to immediately explore the app with pre-populated financial data, active charts, and budgets!

---

## 🔥 Firebase Configuration Guide

The project includes an offline-safe configuration template and runs completely offline without requiring active Firebase credentials. To connect your own Firebase project:

1. Visit the [Firebase Console](https://console.firebase.google.com/).
2. Create a new Firebase project (e.g., `SmartExpenseTracker`).
3. Add an Android app with package name:
   ```
   com.example.expensetracker
   ```
4. Download the generated `google-services.json`.
5. Replace the placeholder file at:
   ```
   app/google-services.json
   ```
6. In Firebase Console:
   - Enable **Authentication** -> **Email/Password**.
   - Enable **Cloud Firestore** -> Create database in test mode or with rules:
     ```javascript
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /users/{userId}/{document=**} {
           allow read, write: if request.auth != null && request.auth.uid == userId;
         }
       }
     }
     ```
7. Rebuild and run the app. Transactions and budgets will automatically sync with your Cloud Firestore!

---

## 📄 Data Export (PDF & CSV)

### PDF Statement Generation
- Built with Android's native `android.graphics.pdf.PdfDocument`.
- Generates a styled A4 document containing:
  - Header branding and user statement metadata.
  - KPI summary cards (Total Income, Total Expenses, Net Savings).
  - Itemized transaction log with dates, types, categories, notes, and colored amounts.
  - Confidentiality footer.
- Opens via Android system share sheet to send directly to WhatsApp, Drive, Gmail, or a PDF reader.

### CSV Export
- Generates standard CSV table with headers:
  `Transaction ID, Date, Type, Category, Amount, Description`
- Seamlessly importable into Microsoft Excel, Google Sheets, or Apple Numbers.

---

## 🔮 Future Enhancements

- [ ] OCR receipt scanning with ML Kit to auto-populate transaction amounts.
- [ ] Push notifications using Android WorkManager for daily budget reminders.
- [ ] Biometric Fingerprint / Face Unlock authentication.
- [ ] Recurring subscription tracking and bill payment reminders.
- [ ] Multi-wallet support (Cash, Bank Accounts, Credit Cards).

---

## 👨‍💻 Author & Project Information

- **Project**: Smart Expense & Budget Management App
- **Program**: B.Tech Computer Science & Engineering (Capstone Project)
- **Domain**: Native Android Application Development (Java)
- **License**: MIT License - open for educational and portfolio demonstration.
