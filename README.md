# MoneyBoard

MoneyBoard is a local-first Android personal-finance tracker built with Kotlin and Jetpack Compose. The project focuses on accounts, categories, transactions, monthly planning, and a dashboard that keeps financial data structured and traceable.

## Features

- Account management
- Income and expense categories
- Transaction tracking
- Monthly planning and allocation
- Finance dashboard and summaries
- Room persistence layer
- Seed/default financial categories
- Repository-based data access
- Integer-based money modelling to avoid floating-point errors
- Local-first storage with Android backup disabled

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Room
- KSP
- Coroutines / Flow
- Gradle Kotlin DSL
- JDK 17

## Architecture

The repository separates the finance domain from persistence and UI. Current source includes Room DAOs and repositories for accounts, categories, and transactions, plus dashboard/planning work.

```text
app/src/main/java/com/premraj/moneyboard/
├── core/
│   ├── data/
│   ├── database/
│   └── domain/
└── ui/
```

## Build

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Development Notes

The root-level numbered project documents describe the implementation sequence and architecture decisions for the MoneyBoard build series.

## Repository

https://github.com/prem704raj/MoneyBoard
