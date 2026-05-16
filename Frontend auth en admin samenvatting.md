# Frontend Auth & Admin Pagina – Samenvatting

## Wat er gedaan is

### 1. Auth modellen & service
- **`Frontend/src/app/models/auth.model.ts`** – interfaces `LoginRequest`, `RegisterRequest`, `AuthResponse`
- **`Frontend/src/app/services/auth.service.ts`**:
  - `login()` en `register()` roepen respectievelijk `POST /api/auth/login` en `POST /api/auth/register` aan
  - Token wordt opgeslagen in `localStorage` met key `auth_token`
  - `logout()` verwijdert token en navigeert naar `/login`
  - `isLoggedIn` en `isAdmin` zijn Angular **signals**
  - `checkAdmin()` decodeert JWT-payload via `atob(token.split('.')[1])` en controleert `payload.role === 'ROLE_ADMIN'`

### 2. JWT Interceptor
- **`Frontend/src/app/interceptors/auth.interceptor.ts`**:
  - `HttpInterceptorFn` die `Authorization: Bearer <token>` toevoegt aan elk uitgaand verzoek als er een token aanwezig is
  - Geregistreerd in `app.config.ts` via `provideHttpClient(withInterceptors([authInterceptor]))`

### 3. Route Guards
- **`Frontend/src/app/guards/auth.guard.ts`**:
  - `authGuard` – stuurt door naar `/login` als gebruiker niet ingelogd is
  - `adminGuard` – stuurt door naar `/login` als gebruiker geen admin is

### 4. Login pagina
- **`Frontend/src/app/pages/login/`** (login.ts, login.html, login.scss):
  - Formulier met e-mail en wachtwoord
  - Foutmelding bij mislukte login
  - Bij succesvolle login: admins naar `/admin`, gewone gebruikers naar `/`

### 5. ProductService uitgebreid
- **`Frontend/src/app/services/product.service.ts`** – CRUD-methoden toegevoegd:
  - `create(request)` – `POST /api/products`
  - `update(id, request)` – `PUT /api/products/{id}`
  - `delete(id)` – `DELETE /api/products/{id}`
  - `addImage(productId, imageUrl)` – `POST /api/products/{id}/images`
  - `deleteImage(productId, imageId)` – `DELETE /api/products/{id}/images/{imageId}`
  - `interface ProductRequest` geëxporteerd (name, description, price, stock)

### 6. Admin pagina
- **`Frontend/src/app/pages/admin/`** (admin.ts, admin.html, admin.scss):
  - Productenlijst in tabel met: ID, naam, prijs, voorraad, aantal afbeeldingen
  - **Toevoegen**: knop opent formulier → `POST /api/products`
  - **Bewerken**: knop vult formulier → `PUT /api/products/{id}`
  - **Verwijderen**: knop met `confirm()` → `DELETE /api/products/{id}`
  - **Afbeeldingen beheren**: inline panel per product – bekijken, URL toevoegen, verwijderen
  - Angular signals voor reactieve state: `products`, `loading`, `error`, `successMsg`, `showForm`, `editingProduct`, `selectedProductForImage`
  - Succesmelding verdwijnt automatisch na 3 seconden
  - Admin-component wordt **lazy loaded** (eigen chunk in productie-build)

### 7. Routing
- **`Frontend/src/app/app.routes.ts`**:
  - `{ path: 'login' → LoginComponent }`
  - `{ path: 'admin', canActivate: [adminGuard], loadComponent: () => AdminComponent }`

## Git commits (in volgorde)
| Hash | Bericht |
|---|---|
| `dd8a855` | fix: use environment-based API URL for production deployment |
| `6d9e2f7` | feat: add auth service, JWT interceptor, auth guard and login page |
| `fa5ce27` | feat: add admin CRUD page with product and image management |

## Productie build output
- Main chunk: ~294 kB
- Admin lazy chunk: ~12.7 kB (wordt pas geladen als admin-route bezocht wordt)

## Status na deze sessie
- ✅ Login werkt (admin → /admin, user → /)
- ✅ JWT wordt automatisch meegestuurd via interceptor
- ✅ Admin portaal volledig functioneel (CRUD producten + afbeeldingen)
- ✅ Frontend gebouwd en gedeployed op VPS (178.104.192.234)
