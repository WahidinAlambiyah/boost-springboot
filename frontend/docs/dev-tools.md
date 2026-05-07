# Frontend Dev Tools

## Scope

Halaman `/dev` adalah area khusus untuk development dan demo internal. Gunakan halaman ini untuk memvalidasi pola UI, komponen reusable, dan contoh flow CRUD sebelum pola tersebut dipakai di modul production.

Dev Tools bukan bagian dari pengalaman pengguna production. Jika halaman atau link Dev Tools belum siap untuk production, jangan tampilkan link menuju `/dev` di environment production.

## Access Control

Akses route `/dev` dikontrol dengan `RequirePermission` pada layout Dev Tools. Permission target yang diharapkan adalah `DEV_TOOLS_READ`.

Saat ini frontend masih memakai fallback sementara `ROLE_READ`/`USER_READ` agar demo internal tetap bisa diakses sebelum backend menyediakan authority khusus `DEV_TOOLS_READ`. Fallback ini harus dihapus setelah backend/auth menu mengirimkan permission `DEV_TOOLS_READ`.

Untuk aksi tulis pada demo CRUD atau komponen, permission target yang disarankan adalah `DEV_TOOLS_WRITE`. Jika masih ada fallback `ROLE_READ`/`USER_READ` atau permission dummy lain, perlakukan sebagai kompatibilitas sementara sampai backend menyediakan authority Dev Tools yang final.

## Menu dan Link Production

Sidebar utama tidak meng-hardcode link Dev Tools karena daftar menu utama berasal dari backend/auth menu. Jika `/dev` perlu muncul di navigation, backend/auth menu harus mengirim item menu yang sesuai dan hanya untuk user/environment yang tepat.

Quick link atau entry point lain yang dibuat lokal di frontend harus tetap dicek dengan permission dan sebaiknya disembunyikan di production bila belum siap. Jangan mengandalkan hiding sebagai satu-satunya proteksi; route `/dev` tetap harus dijaga oleh `RequirePermission`.

## Env Flag `NEXT_PUBLIC_ENABLE_DEV_TOOLS`

Dev Tools dikontrol oleh helper `ENABLE_DEV_TOOLS` di `src/lib/dev-tools.ts`:

```ts
export const ENABLE_DEV_TOOLS =
  process.env.NEXT_PUBLIC_ENABLE_DEV_TOOLS === "true" || process.env.NODE_ENV !== "production";
```

Perilaku flag:

- `NEXT_PUBLIC_ENABLE_DEV_TOOLS=true` mengaktifkan link dan route Dev Tools di semua environment.
- Di non-production (`NODE_ENV !== "production"`), Dev Tools aktif secara default supaya demo internal tetap mudah dipakai saat development/test.
- Di production, Dev Tools nonaktif kecuali `NEXT_PUBLIC_ENABLE_DEV_TOOLS` diset eksplisit ke `true`.

Contoh konfigurasi production untuk menyembunyikan dan menutup route Dev Tools:

```env
NEXT_PUBLIC_ENABLE_DEV_TOOLS=false
```

Entry point lokal seperti dashboard quick link harus memakai `ENABLE_DEV_TOOLS` sebelum menambahkan link `/dev`. Route `/dev` dan seluruh route nested `/dev/*` juga harus memanggil behavior not found saat `ENABLE_DEV_TOOLS` false agar halaman demo tidak dapat dibuka langsung ketika fitur dinonaktifkan.

Env flag ini hanya feature gate untuk visibility/availability Dev Tools. Env flag bukan pengganti authorization: route Dev Tools tetap harus dibungkus `RequirePermission`, dan user tetap membutuhkan permission seperti `DEV_TOOLS_READ` atau fallback sementara yang masih berlaku.

Rekomendasi default:

- Development/demo internal: tidak perlu set flag karena non-production aktif otomatis; set `NEXT_PUBLIC_ENABLE_DEV_TOOLS=true` jika perlu menegaskan perilaku tersebut di environment tertentu.
- Production: biarkan kosong atau set `NEXT_PUBLIC_ENABLE_DEV_TOOLS=false` sampai halaman, permission backend, dan menu backend siap.

## Checklist Dev Tools

- [ ] Route `/dev` dibungkus `RequirePermission`.
- [ ] Permission target memakai `DEV_TOOLS_READ`/`DEV_TOOLS_WRITE` jika backend sudah tersedia.
- [ ] Fallback `ROLE_READ`/`USER_READ` diberi catatan sementara dan dihapus setelah backend siap.
- [ ] Link Dev Tools tidak ditampilkan di production jika belum siap.
- [ ] Sidebar utama tetap mengikuti backend/auth menu dan tidak menambahkan item `/dev` secara hardcode.
