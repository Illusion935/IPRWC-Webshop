# Products — Opzet & Samenvatting

## Backend: Entiteiten

### Product
JPA-entiteit (`products` tabel) met de volgende velden:
- `id` — auto-increment primary key
- `name` — verplicht
- `description` — TEXT
- `price` — BigDecimal (precisie 10, schaal 2)
- `stock` — Integer

### ProductImage
Aparte JPA-entiteit (`product_images` tabel), gekoppeld aan een product via een foreign key:
- `id` — auto-increment primary key
- `imageUrl` — de volledige URL naar de afbeelding (bijv. Cloudinary)
- `productName` — naam van het product (denormalized voor gemak)
- `product` — `@ManyToOne` naar Product, `@JsonBackReference` om circulaire serialisatie te voorkomen

De relatie aan Product-kant is `@OneToMany` met `FetchType.EAGER` en `@JsonManagedReference`. `FetchType.EAGER` was nodig omdat de standaard LAZY loading een `LazyInitializationException` veroorzaakte buiten een actieve Hibernate-sessie.

---

## Backend: Service & Controller

`ProductService` heeft `@Transactional` op klassenniveau, wat de Hibernate-sessie open houdt voor de duur van elke methode. Zonder dit crashed EAGER loading alsnog buiten een transactie.

Endpoints:
| Methode | URL | Toegang |
|---|---|---|
| GET | `/api/products` | Publiek |
| GET | `/api/products/{id}` | Publiek |
| POST | `/api/products` | ADMIN |
| PUT | `/api/products/{id}` | ADMIN |
| DELETE | `/api/products/{id}` | ADMIN |
| POST | `/api/products/{id}/images` | ADMIN |
| POST | `/api/products/{id}/images/bulk` | ADMIN |
| DELETE | `/api/products/{id}/images/{imageId}` | ADMIN |

DTO's: `ProductRequest` (name, description, price, stock) en `ProductImageRequest` (imageUrl).

---

## Afbeeldingen via Cloudinary

Afbeeldingen worden extern gehost op Cloudinary (cloud name: `dlxwua3d6`). De backend slaat alleen de volledige Cloudinary-URL op in de database — er wordt niets lokaal opgeslagen. Uploaden naar Cloudinary gebeurt handmatig of via de Cloudinary API; de backend ontvangt enkel de resulterende URL via `POST /api/products/{id}/images`.

Testafbeeldingen:
- `https://res.cloudinary.com/dlxwua3d6/image/upload/test_tshirt_front_bfehad`
- `https://res.cloudinary.com/dlxwua3d6/image/upload/test_tshirt_back_nhrwdo`

---

## CORS

Angular draait op `localhost:4200`, de backend op `localhost:8080`. Browsers blokkeren standaard cross-origin requests. In `SecurityConfig` is een `CorsConfigurationSource` bean geconfigureerd:

```java
config.setAllowedOrigins(List.of("http://localhost:4200"));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
config.setAllowedHeaders(List.of("*"));
config.setAllowCredentials(true);
```

Belangrijk: `.cors(...)` moet vóór `.csrf(...)` in de security filter chain staan, anders worden OPTIONS preflight-requests geblokkeerd voordat CORS headers worden toegevoegd.

---

## Frontend: Modellen & Service

`product.model.ts` definieert twee interfaces: `Product` en `ProductImage`. De `ProductService` heeft één injectable HTTP-service met `getAll()` en `getById()` als publieke methoden die een `Observable` teruggeven.

---

## Frontend: Signals voor state in de template

Angular 21 wordt standaard zonder Zone.js gegenereerd. Zone.js was voorheen verantwoordelijk voor het "patchen" van async-operaties (fetch, Promise, XHR) zodat Angular wist wanneer change detection moest worden uitgevoerd. Zonder Zone.js triggert een gewone variabele-toewijzing in een `subscribe`-callback geen re-render van de template.

De oplossing is **Angular Signals**. Een handige vuistregel:

> *Gebruik RxJS voor events en asynchrone activiteit. Zodra je data in de template wilt tonen, converteer je je Observable-stream naar een Signal. Geef jezelf geen schrijfbaar signal te vroeg — dan bestaat de verleiding om imperatieve code te schrijven.*

In de `HomeComponent` wordt de Observable van `ProductService` via `subscribe` omgezet naar een signal:

```typescript
products = signal<Product[]>([]);

ngOnInit(): void {
  this.productService.getAll().subscribe({
    next: data => this.products.set(data),
    error: err => console.error('Fout bij ophalen producten:', err)
  });
}
```

In de template wordt de signal aangeroepen als functie om de huidige waarde op te halen:

```html
<div class="product-card" *ngFor="let product of products()">
```

Angular registreert de template als "luisteraar" van het signal op het moment van aanroep. Zodra `.set()` wordt aangeroepen, weet Angular exact welke templates opnieuw gerenderd moeten worden — zonder Zone.js.
