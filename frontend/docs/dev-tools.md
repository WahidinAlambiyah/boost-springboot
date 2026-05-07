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

## Opsi Env Flag untuk Hiding di Production

Jika butuh memastikan link Dev Tools tidak tampil di production, gunakan env flag publik, misalnya:

```env
NEXT_PUBLIC_ENABLE_DEV_TOOLS=false
```

Contoh pemakaian untuk entry point lokal seperti dashboard quick link:

```ts
const enableDevTools = process.env.NEXT_PUBLIC_ENABLE_DEV_TOOLS === "true";

const moduleLinks = [
  // module lain...
  ...(enableDevTools
    ? [
        {
          label: "Developer Tools",
          href: "/dev",
          permissions: ["DEV_TOOLS_READ", "ROLE_READ", "USER_READ"],
        },
      ]
    : []),
];
```

Rekomendasi default:

- Development/demo internal: set `NEXT_PUBLIC_ENABLE_DEV_TOOLS=true` jika link lokal perlu ditampilkan.
- Production: set `NEXT_PUBLIC_ENABLE_DEV_TOOLS=false` sampai halaman, permission backend, dan menu backend siap.

## Checklist Dev Tools

- [ ] Route `/dev` dibungkus `RequirePermission`.
- [ ] Permission target memakai `DEV_TOOLS_READ`/`DEV_TOOLS_WRITE` jika backend sudah tersedia.
- [ ] Fallback `ROLE_READ`/`USER_READ` diberi catatan sementara dan dihapus setelah backend siap.
- [ ] Link Dev Tools tidak ditampilkan di production jika belum siap.
- [ ] Sidebar utama tetap mengikuti backend/auth menu dan tidak menambahkan item `/dev` secara hardcode.
