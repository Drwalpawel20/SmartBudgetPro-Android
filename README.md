# SmartBudgetPro

**SmartBudgetPro** to natywna aplikacja na Androida do zarządzania budżetem osobistym. Pozwala rejestrować wydatki, porządkować je w kategoriach, analizować statystyki miesięczne, śledzić inwestycje i eksportować dane do pliku CSV.

![Platform](https://img.shields.io/badge/platforma-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/język-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Architecture](https://img.shields.io/badge/architektura-MVVM-blue)
![DI](https://img.shields.io/badge/DI-Hilt-orange)
![Database](https://img.shields.io/badge/baza_danych-Room-informational)

---

## Spis treści

- [Funkcje](#funkcje)
- [Zrzuty ekranu](#zrzuty-ekranu)
- [Stos technologiczny](#stos-technologiczny)
- [Architektura](#architektura)
- [Struktura projektu](#struktura-projektu)
- [Uruchomienie projektu](#uruchomienie-projektu)
- [Konfiguracja](#konfiguracja)
- [Testy](#testy)
- [Plany rozwoju](#plany-rozwoju)
- [Licencja](#licencja)
- [Autor](#autor)

---

## Funkcje

**Konto użytkownika**
- Rejestracja i logowanie do aplikacji
- Odzyskiwanie hasła za pomocą kodu resetującego wysyłanego e-mailem

**Wydatki**
- Dodawanie wydatków przez okno dialogowe
- Lista transakcji z możliwością przeglądania historii
- Gotowy zestaw kategorii: jedzenie, transport, rachunki, zdrowie, edukacja, rozrywka, zakupy, oszczędności i inne (kategorie inicjalizowane przy pierwszym uruchomieniu)

**Budżet**
- Definiowanie budżetów i śledzenie ich wykorzystania
- Powiadomienia systemowe związane z budżetem

**Statystyki**
- Dashboard z podsumowaniem bilansu
- Miesięczne zestawienia i analiza wydatków według kategorii
- Wykresy prezentujące strukturę wydatków

**Inwestycje**
- Osobna zakładka z listą inwestycji
- Dane pobierane z zewnętrznego API (Retrofit + OkHttp)

**Dane i ustawienia**
- Eksport danych do pliku **CSV** i udostępnianie go z poziomu aplikacji
- Ekran ustawień
- Lokalne przechowywanie danych w bazie Room (praca offline)

## Zrzuty ekranu

> Dodaj własne zrzuty ekranu do katalogu `docs/screenshots/` i zaktualizuj ścieżki poniżej.

| Logowanie | Dashboard | Wydatki |
|:---:|:---:|:---:|
| ![Logowanie](docs/screenshots/login.png) | ![Dashboard](docs/screenshots/dashboard.png) | ![Wydatki](docs/screenshots/expenses.png) |

| Statystyki | Inwestycje | Ustawienia |
|:---:|:---:|:---:|
| ![Statystyki](docs/screenshots/stats.png) | ![Inwestycje](docs/screenshots/investment.png) | ![Ustawienia](docs/screenshots/settings.png) |

## Stos technologiczny

| Obszar | Technologia |
|---|---|
| Język | Kotlin |
| UI | Android Views (XML) z View/Data Binding, Material Components |
| Nawigacja | Jetpack Navigation (nav graph, Safe Args) + Bottom Navigation |
| Architektura | MVVM (ViewModel + Repository) |
| Wstrzykiwanie zależności | Dagger Hilt (przetwarzanie adnotacji przez KAPT) |
| Baza danych | Room (SQLite) |
| Sieć | Retrofit, OkHttp, HttpLoggingInterceptor |
| Asynchroniczność | Kotlin Coroutines, Flow |
| Budowanie | Gradle (Kotlin DSL), Version Catalog (`libs.versions.toml`) |

## Architektura

Aplikacja opiera się na wzorcu **MVVM** z jednokierunkowym przepływem danych:

```
Fragment / Activity  →  ViewModel  →  FinanceRepository  →  Room DAO  /  Retrofit API
        (UI)            (stan UI)       (logika danych)       (źródła danych)
```

- **UI** – fragmenty i aktywności odpowiadają wyłącznie za prezentację i obsługę zdarzeń.
- **ViewModel** – udostępnia stan ekranu i izoluje UI od warstwy danych.
- **Repository** – `FinanceRepository` jest jedynym punktem dostępu do danych (wydatki, kategorie, budżety, miesięczne podsumowania).
- **DI** – moduły Hilt (`AppModule`, `NetworkModule`) dostarczają bazę danych, repozytorium, klienta HTTP i API.

## Struktura projektu

```
SmartBudgetPro/
├── app/
│   └── src/main/
│       ├── java/com/example/smartbudgetpro/
│       │   ├── data/
│       │   │   ├── dao/            # BudgetDao, CategoryDao, ExpenseDao
│       │   │   ├── entity/         # Budget, Category, Expense
│       │   │   ├── repository/     # FinanceRepository
│       │   │   └── AppDatabase.kt  # konfiguracja Room
│       │   ├── di/                 # AppModule, NetworkModule (Hilt)
│       │   ├── network/            # InvestmentApi (Retrofit)
│       │   ├── ui/
│       │   │   ├── adapter/        # ExpenseAdapter, InvestmentAdapter
│       │   │   ├── dashboard/      # ekran główny
│       │   │   ├── expenses/       # lista i dodawanie wydatków
│       │   │   ├── investment/     # inwestycje
│       │   │   ├── stats/          # statystyki i wykresy
│       │   │   └── settings/       # ustawienia
│       │   ├── utils/              # CsvExporter, NotificationHelper, StatsCalculator
│       │   ├── LoginActivity.kt
│       │   ├── RegisterActivity.kt
│       │   ├── MainActivity.kt
│       │   └── MyApplication.kt
│       └── res/                    # layouty, nawigacja, ikony kategorii, motywy
├── gradle/                         # wrapper i katalog wersji bibliotek
├── build.gradle.kts
└── settings.gradle.kts
```

## Uruchomienie projektu

### Wymagania

- Android Studio w aktualnej stabilnej wersji
- JDK 17 lub nowszy (zazwyczaj dołączony do Android Studio)
- Android SDK zainstalowany przez SDK Managera
- Emulator lub fizyczne urządzenie z włączonym debugowaniem USB

### Kroki

```bash
git clone https://github.com/Drwalpawel20/SmartBudgetPro-Android.git
cd SmartBudgetPro-Android
```

1. Otwórz katalog projektu w Android Studio (**File → Open**).
2. Poczekaj na zakończenie synchronizacji Gradle.
3. Wybierz urządzenie i uruchom aplikację (**Run ▶**).

Budowanie z wiersza poleceń:

```bash
./gradlew assembleDebug        # Linux / macOS
gradlew.bat assembleDebug      # Windows
```

Plik APK pojawi się w `app/build/outputs/apk/debug/`.

## Konfiguracja

Plik `local.properties` zawiera lokalną ścieżkę do Android SDK. Android Studio generuje go automatycznie i **nie powinien trafiać do repozytorium**.

Funkcje korzystające z usług zewnętrznych (wysyłka kodu resetującego hasło e-mailem oraz API inwestycji) wymagają własnej konfiguracji. **Nie umieszczaj w kodzie ani w repozytorium haseł, kluczy API ani danych dostępowych do skrzynki pocztowej.** Przechowuj je w `local.properties` lub zmiennych środowiskowych i odczytuj przez `BuildConfig`.

## Testy

```bash
./gradlew test                  # testy jednostkowe
./gradlew connectedAndroidTest  # testy instrumentalne (wymagają urządzenia)
```

## Plany rozwoju

- [ ] Transakcje cykliczne
- [ ] Kopia zapasowa i synchronizacja w chmurze
- [ ] Obsługa wielu walut
- [ ] Widżet na ekran główny
- [ ] Tryb ciemny
- [ ] Rozbudowane testy jednostkowe i UI

## Licencja

Uzupełnij informację o licencji (np. MIT) i dodaj plik `LICENSE` w katalogu głównym repozytorium.

## Autor

**Drwalpawel20** – [github.com/Drwalpawel20](https://github.com/Drwalpawel20)
