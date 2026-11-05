# 🌸 DinsDiary

**Your Little Period & Health Diary.**

Aplikasi Android _offline-first_ untuk mencatat siklus haid, gejala harian, dan _mood_ — dengan estimasi siklus yang transparan dan deterministik. Tanpa akun, tanpa internet untuk fitur inti, data tetap di perangkat.

![Build](https://github.com/zealish/DinsDiary/actions/workflows/build.yml/badge.svg)
![Platform](https://img.shields.io/badge/platform-Android%2026%2B-3DDC84)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)

> ⚠️ **Bukan alat medis.** Semua prediksi tanggal adalah _estimasi_, bukan jaminan medis, bukan konfirmasi ovulasi, dan tidak boleh dipakai sebagai kontrasepsi. Lihat [PRD.md](PRD.md) §5.

## ✨ Fitur

| Area | Status | Keterangan |
| --- | --- | --- |
| Onboarding + profil | ✅ MVP | Nama opsional, preferensi siklus, bisa dilewati |
| Dashboard home | ✅ MVP | Hari siklus, status, prediksi haid berikut / status data-kurang |
| Pencatatan haid | ✅ MVP | CRUD periode + aliran harian, konfirmasi hapus, deteksi duplikat/overlap |
| Kalender | ✅ MVP | Aktual vs estimasi dibedakan glyph + legenda, detail harian |
| Mesin siklus | ✅ MVP | Pure Kotlin, median robust, confidence + penjelasan, berversi |
| Jurnal harian | ✅ MVP | Mood, multi-gejala + severity, energi/tidur/catatan |
| Estimasi fertilitas | ✅ MVP | Jendela subur + ovulasi dari asumsi fase luteal, dengan disclaimer |
| Insights | ✅ MVP | Riwayat panjang siklus, durasi haid, pola mood/gejala (observasi saja) |
| Profil + preferensi | ✅ MVP | Display name, preferensi siklus, tema System/Light/Dark |
| Pengingat lokal | ✅ MVP | WorkManager, teks notifikasi privat generik |
| Privasi & data | ✅ MVP | Ekspor JSON, hapus-semua-data terkonfirmasi |
| Bahasa | ✅ MVP | 🇮🇩 Indonesia default, 🇬🇧 English — switch in-app, offline penuh |
| App lock | 🔜 | Ditandai coming-soon (butuh desain keamanan khusus) |

## 🛠️ Teknologi

- **Kotlin** 2.0.21 · **Jetpack Compose** + Material 3 · Clean Architecture + MVVM
- **Hilt** (DI) · **Room** / SQLite (source of truth) · **DataStore** (preferensi)
- Coroutines + Flow · Navigation Compose · **WorkManager** (pengingat)
- Kotlinx Serialization · JUnit + MockK + Room tests + Compose UI tests
- `applicationId` `com.zealish.dinsdiary` · minSdk 26 · target/compileSdk 36 · Java 17

## 🚀 Memulai

Prasyarat: **JDK 17** + Android SDK (API 36).

```bash
git clone https://github.com/zealish/DinsDiary.git
cd DinsDiary

# Jalankan di emulator/perangkat (atau buka folder ini di Android Studio)
./gradlew :app:assembleDebug

# Jalankan unit tests
./gradlew :app:testDebugUnitTest
```

Rilis bertanda (`git tag v1.x.y` → push tag) membangun APK/AAB lewat [release.yml](.github/workflows/release.yml); signing keystore diambil dari repo secrets bila tersedia, kalau tidak build tetap jalan tanpa signing.

## 📁 Struktur proyek

```text
app/src/main/java/com/zealish/dinsdiary/
├── data/        # Room entities + DAO, repository impl, mappers, DataStore, WorkManager
├── domain/      # Pure Kotlin: models, repository interfaces, use cases, cycle engine
├── feature/     # Compose screens per fitur: onboarding, home, calendar, period,
│                #   journal, insights, fertility, reminders, profile, privacy
├── navigation/  # Nav graph + routes (type-safe)
└── ui/          # Theme (Material 3, light/dark), komponen bersama
```

## 📚 Dokumentasi — urutan baca wajib

> `PRD.md` adalah **sumber kebenaran produk**. Dokumen pendukung menjelaskan _cara implementasi_ dan tidak boleh mengubah scope produk secara diam-diam. Konflik → selesaikan eksplisit + catat di `CHANGELOG.md`.

1. [`PRD.md`](PRD.md) — goals, scope, prioritas, safety boundaries, acceptance criteria
2. [`AGENTS.md`](AGENTS.md) — aturan wajib kontributor (termasuk AI agents)
3. Sesuai kebutuhan tugas:
   - [`ARCHITECTURE.md`](ARCHITECTURE.md) — layers, modul, data flow
   - [`DATABASE_SCHEMA.md`](DATABASE_SCHEMA.md) — entities, relasi, aturan migrasi
   - [`CYCLE_ENGINE.md`](CYCLE_ENGINE.md) — kalkulasi siklus, confidence, test cases
   - [`UI_UX_GUIDELINES.md`](UI_UX_GUIDELINES.md) — design system, navigasi, aksesibilitas
   - [`CODING_STANDARD.md`](CODING_STANDARD.md) — konvensi Kotlin, Compose, testing, Git
4. [`CHANGELOG.md`](CHANGELOG.md) — riwayat keputusan; **setiap task wajib append checkpoint**

Prinsip inti: offline-first · privacy-first (tanpa akun) · Room = source of truth · catatan aktual ≠ estimasi · `LocalDate` untuk tanggal haid · clock ter-inject untuk test deterministik · tidak ada data kesehatan sensitif di log.

## 🤝 Kontribusi

1. Baca `PRD.md` → identifikasi requirement + acceptance criteria.
2. Ikuti batasan layer (domain murni Kotlin, tanpa Android/Compose).
3. Tambah/ubah tests untuk setiap aturan validasi & edge case tanggal.
4. Jalankan `./gradlew :app:assembleDebug :app:testDebugUnitTest` sebelum push.
5. Append checkpoint ke `CHANGELOG.md` (format di `AGENTS.md` §12).
