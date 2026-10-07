# MoneyBoard

MoneyBoard is a **local-first** Android personal-finance tracker built with **Kotlin** and **Jetpack Compose**. It focuses on structured financial planning, transaction tracking, and a dashboard that keeps your money data traceable and accurate.

---

## Current Status & Feature Matrix

### Implemented & Verified
- **Dashboard** — Plan vs Actual monthly view with month navigation and 12-month projections
- **Domain & Persistence Core** — Integer minor-unit `Money` arithmetic with overflow protection and ISO-4217 validation
- **Room Persistence** — Typed Room 3 DAOs, foreign key constraints, composite indices, and version 2 schema
- **Centralized Formatting** — `MoneyFormatter` respecting currency fraction digits and locale grouping
- **Test Suite** — 48+ unit tests covering financial invariants, arithmetic, date boundaries, and projections
- **GitHub Actions CI** — Unit tests, Android test assembly, release lint, APK/AAB build gates

### In Active Development (v0.4.0 Milestone)
- **Transaction CRUD** — Add, edit, list, and soft-delete transactions with snackbar undo
- **Account Management** — Account creation, balance inspection, and archiving
- **Monthly Plan Editor** — Income and category budget authoring
- **Database Migration Test** — Automated `MigrationTestHelper` test for Room v1 → v2
- **Data Export** — Local export / backup for device migration

## Tech Stack

| Layer        | Technology                                     |
| ------------ | ---------------------------------------------- |
| Language     | Kotlin 2.3                                     |
| UI           | Jetpack Compose + Material 3                   |
| Persistence  | Room 3 (AndroidSQLite driver)                  |
| Async        | Coroutines / Flow                              |
| Build        | Gradle Kotlin DSL, KSP, Version Catalogs       |
| DI           | Manual (AppContainer)                          |
| CI           | GitHub Actions                                 |
| Target       | JDK 17, Android SDK 35, minSdk 26              |

## Architecture

```text
app/src/main/java/com/premraj/moneyboard/
├── core/
│   ├── common/          # IdProvider, TimeProvider
│   ├── dashboard/       # DashboardCalculator, ObserveMonthlyDashboardUseCase
│   ├── data/
│   │   ├── mapper/      # Entity ↔ Domain mappers
│   │   └── repository/  # Room-backed repository implementations
│   ├── database/
│   │   ├── dao/         # Room DAOs (Account, Category, Transaction, MonthlyPlan)
│   │   ├── entity/      # Room entities
│   │   └── projection/  # CategoryTotalRow
│   ├── designsystem/    # Colors, Typography, Theme, Dimens
│   └── domain/
│       ├── model/       # Money, Account, Category, Transaction, Plans, Metrics
│       ├── repository/  # Repository interfaces + command objects
│       └── util/        # DateRanges, YearMonthStorage
├── debug/               # ReferenceDemoPlanSeeder (debug builds only)
├── di/                  # AppContainer (manual DI)
├── feature/
│   └── dashboard/       # ViewModel, Screen, UI models, components
└── MoneyBoardApplication.kt
```

### Key Design Decisions

- **Integer money**: All monetary values stored as `Long` minor units (e.g., paise for INR). Overflow-safe via `Math.addExact`/`multiplyExact`.
- **No Hilt**: Manual DI via `AppContainer` — simple, auditable, zero reflection.
- **Soft delete**: Transactions are soft-deleted (preserving audit trail), not hard-deleted.
- **Currency validation**: `java.util.Currency` validates ISO-4217 at construction time.
- **No cloud sync**: Intentionally local-first. Backup responsibility is on the user.

## Build

```bash
# macOS / Linux
./gradlew assembleDebug

# Windows
.\gradlew.bat assembleDebug
```

### Run unit tests

```bash
./gradlew testDebugUnitTest
```

### Run lint

```bash
./gradlew lintDebug
```

## Testing

| Test Suite                      | Type           | Coverage                              |
| ------------------------------- | -------------- | ------------------------------------- |
| `MoneyTest`                     | Unit           | Money arithmetic, precision, currency |
| `MoneyExtendedTest`             | Unit           | Multi-currency, overflow, formatting  |
| `DashboardCalculatorTest`       | Unit           | Metrics, savings rate, annualization  |
| `DomainStorageEnumTest`         | Unit           | Enum round-trip safety                |
| `DateRangesTest`                | Unit           | Leap year, month boundaries           |
| `YearMonthStorageTest`          | Unit           | Storage key encoding/decoding         |
| `FinanceTransactionTest`        | Unit           | Domain invariants, validation         |
| `MoneyBoardDatabaseTest`        | Instrumented   | Room DAO operations, soft delete      |
| `DatabaseInitializerTest`       | Instrumented   | Seed idempotency                      |
| `RoomTransactionRepositoryTest` | Instrumented   | Repository contract, month isolation  |

## CI
 
GitHub Actions workflow at `.github/workflows/android.yml` runs on every push and PR:
1. Unit tests (`testDebugUnitTest`)
2. Android test assembly (`assembleDebugAndroidTest`)
3. Lint checks (`lintDebug`, `lintRelease`)
4. Build verification (`assembleDebug`, `assembleRelease`, `bundleRelease`)

## Repository

https://github.com/prem704raj/MoneyBoard

## License

Private — see repository for details.
