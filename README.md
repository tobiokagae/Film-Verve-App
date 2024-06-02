# FilmVerve

_Sebuah aplikasi mobile yang dirancang untuk menyediakan informasi terkini tentang movie kepada user dengan movie yang terus di update._


**FITUR-FITUR**
1. Menampilkan Informasi Movie
2. Menampilkan Movie yang akan tayang, yang sedang tayang, yang populer, dan yang sedang trending.
3. Pencarian Movie.
4. Menambahkan Movie ke dalam Watchlist
5. Login dan Register akun.
6. Hapus akun.

**SPESIFIKASI TEKNIS**
1. Activity : 4 Activity (Main Activity, Login Activity, Register Activity, Detail Activity)
2. Intent: Berpindah Activity, Mendapatkan data tambahan dari Activity sebelumnya.
3. RecyclerView: Untuk menampilkan data seperti daftar film populer film yang sedang trend, daftar watchlist, dll.
4. Fragment & Navigation: Digunakan dalam bottom navigation untuk menavigasi antar fragment (Home Fragment, Search Fragment, Watchlist Fragment, Profile Fragment).
5. Background Thread (Executor dan Handler) :
     - Penggunaan ExecutorService
     - Penggunaan Handler
6. Networking: Menggunakan API untuk ditarik data data semua mvoie.
7. Local Data Persistent:
     - SQLite : menyimpan data data pengguna yang telah register
     - Shared Preferences : menyimpan informasi login, menyimpan data akun user yang login untuk ditampilkan.





