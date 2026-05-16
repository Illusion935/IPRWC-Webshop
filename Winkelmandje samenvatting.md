# Winkelmandje — Opzet & Samenvatting

## Wat er gedaan is (16 mei 2026)

Het volledige winkelmandje is van nul af aan geïmplementeerd, zowel in de backend als de frontend. Dit was de grootste ontbrekende eis voor het assessment.

---

## Backend

### Nieuwe entiteiten

#### `OrderStatus` (enum)
Twee mogelijke statussen:
- `CART` — winkelmandje dat nog niet afgerekend is
- `PLACED` — geplaatste bestelling

#### `OrderItem`
JPA-entiteit (`order_items` tabel) die één productregel in een bestelling representeert:
- `id` — auto-increment primary key
- `order` — `@ManyToOne` naar Order, `@JsonIgnore` (voorkomt circulaire JSON)
- `product` — `@ManyToOne` naar Product, `FetchType.EAGER`
- `quantity` — aantal stuks

#### `Order` (uitgebreid van leeg naar volledig)
JPA-entiteit (`orders` tabel):
- `id` — auto-increment primary key
- `user` — `@ManyToOne` naar User, `@JsonIgnore`
- `items` — `@OneToMany` naar OrderItem, `CascadeType.ALL`, `FetchType.EAGER`
- `status` — `OrderStatus` enum, standaard `CART`
- `createdAt` — `LocalDateTime`, standaard `now()`

> **Belangrijk:** `FetchType.EAGER` op `items` was noodzakelijk. Met de standaard LAZY loading crashte de JSON-serialisatie buiten de Hibernate-sessie, wat Spring Security ten onrechte als een `401` teruggaf in plaats van een `500`.

### Nieuwe repository's & DTO's

- **`OrderItemRepository`** — `deleteByProductId(Long productId)` voor safe product verwijdering
- **`OrderRepository`** — `findByUserAndStatus(User, OrderStatus)` voor het ophalen van het actieve winkelmandje
- **`AddToCartRequest`** DTO — `productId` en `quantity`

### `OrderService`

Bevat de volledige bedrijfslogica:
| Methode | Beschrijving |
|---------|-------------|
| `getCart()` | Haalt het actieve CART-order op, of maakt een nieuw aan |
| `addToCart(request)` | Voegt een product toe; verhoogt quantity als het product al in het mandje zit |
| `removeFromCart(itemId)` | Verwijdert één regel uit het mandje |
| `clearCart()` | Verwijdert alle items uit het mandje |
| `checkout()` | Zet de status van CART naar PLACED |

De huidige gebruiker wordt bepaald via `SecurityContextHolder.getContext().getAuthentication().getName()` (het e-mailadres uit het JWT-token).

### `OrderController` — endpoints

Alle endpoints vereisen authenticatie (geregeld in `SecurityConfig`):

| Methode | URL | Beschrijving |
|---------|-----|-------------|
| GET | `/api/orders/cart` | Huidig winkelmandje ophalen |
| POST | `/api/orders/cart/items` | Product toevoegen aan mandje |
| DELETE | `/api/orders/cart/items/{itemId}` | Eén item verwijderen |
| DELETE | `/api/orders/cart` | Mandje leegmaken |
| POST | `/api/orders/cart/checkout` | Bestelling plaatsen |

### `ProductService` — bugfix

Bij het verwijderen van een product via de admin werd een FK-constraint fout gegeven als het product in een winkelmandje zat. Opgelost door eerst `orderItemRepository.deleteByProductId(id)` aan te roepen vóór `productRepository.deleteById(id)`.

### `SecurityConfig` — verbetering

`AuthenticationEntryPoint` toegevoegd zodat niet-geauthenticeerde requests een duidelijke `401` terugkrijgen in plaats van een misleidende `403`.

### `DataLoader` — seed data

10 producten worden automatisch aangemaakt bij eerste opstart als de `products` tabel leeg is. Drie producten hebben Cloudinary-afbeeldingen (zelfde als in de VPS-database).

---

## Frontend

### Nieuw model
- **`order.model.ts`** — `Order` en `OrderItem` interfaces

### Nieuwe service
- **`CartService`** (`services/cart.service.ts`) — HTTP-aanroepen naar alle vijf cart-endpoints via `environment.apiUrl`

### Nieuwe pagina: `/cart`
- **`pages/cart/`** (cart.ts, cart.html, cart.scss)
- Toont alle items met productafbeelding, naam, prijs per stuk, aantal en subtotaal
- Totaalbedrag onderaan
- "Verwijderen"-knop per item
- "Mandje leegmaken"-knop met bevestigingsdialoog
- "Afrekenen"-knop die checkout aanroept en een succesbericht toont
- Beveiligd met `authGuard` (niet-ingelogden worden doorgestuurd naar `/login`)

### Home-pagina uitgebreid
- "In winkelmandje" knop toegevoegd per product
- Niet-ingelogde gebruikers worden bij klikken doorgestuurd naar `/login`
- Tijdelijk groen ✓-vinkje na succesvol toevoegen (1,5 seconden)

### Navbar uitgebreid
- 🛒 Winkelmandje-link toegevoegd, zichtbaar als de gebruiker ingelogd is

### Routes
- `/cart` toegevoegd aan `app.routes.ts`, beveiligd met `authGuard`
