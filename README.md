# Tugas Information Retrival 
Dosen Pengampu : Husnul

- Vince Farrel Natanael 618230124 Peran 1
- Filipo Bintang Lautan 6182301004 Peran 2
- Basilius Mozes 6182301032 Peran 3

## Deskripsi Tugas Peran
Peran 1: Data & Indexing Engineer
Fokus pada text preprocessing (tokenization dan stemming) dan membangun struktur data
Inverted Index (Kamus/Term dan Posting List).

Peran 2: Boolean Engine Developer
Fokus membuat query parser dan mengimplementasikan algoritma manipulasi posting list
(intersection dan union) untuk mengeksekusi logika Boolean

Peran 3: Tolerant Retrieval Specialist
Fokus membangun fitur fleksibilitas pencarian, seperti k-gram index untuk menangani
wildcard, atau implementasi perhitungan Levenshtein Distance untuk rekomendasi kata
jika query salah ketik.

## How to Run

1. **Compilation**
   Compile all Java source files in the project root:
   ```bash
   javac *.java
   ```

2. **Execution**
   Run the `Main` class:
   ```bash
   java Main
   ```

3. **Usage**
   Follow the menu prompts to:
   - View a sample of the Inverted Index.
   - Perform Boolean queries with Tolerant Search features.
   - Perform Probabilistic Searches (BIM, BM11, dll).
   - Exit the application.

## Model BM11 (Peran 3 - Tugas 2)
Model BM11 adalah turunan dari *probabilistic retrieval models* yang memperhitungkan bobot term frequency (TF) dan melakukan normalisasi terhadap panjang dokumen secara penuh.

Formula yang diimplementasikan:
1. **Bobot Term ($w_t$):** Dihitung menggunakan logaritma basis 10 terhadap rasio total dokumen dan document frequency (karena tidak ada relevance feedback di awal, $R=0$ dan $r_t=0$). Jika nilai bobot negatif karena kata terlalu umum, nilai akan dibatasi menjadi minimal 0.
2. **Skor Dokumen:** Menggunakan rumus skor BM11 `(ftD * (k + 1) * wt) / (ftD + (k * ld) / lAvg)` di mana `ld` adalah panjang dokumen dan `lAvg` adalah rata-rata panjang seluruh dokumen. Parameter default $k = 1.5$.
