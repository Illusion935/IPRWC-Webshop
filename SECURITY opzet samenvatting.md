# Security — Uitleg & Stappenplan

> Hoe de authenticatie en autorisatie werken in de webshop backend.

---

## Overzicht

De applicatie gebruikt **JWT (JSON Web Tokens)** voor authenticatie. Dit betekent:

- De server slaat **geen sessies op** — elke request is zelfvoorzienend
- De client (browser/app) bewaart het token en stuurt het mee bij elke request
- De server **verifieert het token** bij elke request, zonder database-opslag van sessies

---

## Stap 1 — Gebruikers & Rollen

**Bestand:** `model/User.java`, `model/Role.java`

De `User` klasse implementeert `UserDetails` van Spring Security. Dit is de "taal" die Spring Security spreekt: door dit interface te implementeren weet Spring Security hoe het een gebruiker moet herkennen, welk wachtwoord erbij hoort en welke rechten hij heeft.

```
User
├── id         (auto-increment)
├── email      (uniek, wordt gebruikt als gebruikersnaam)
├── password   (opgeslagen als BCrypt hash, nooit plaintext)
└── role       (enum: USER of ADMIN)
```

De methode `getAuthorities()` vertaalt de rol naar het formaat dat Spring verwacht:

```java
return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
// → "ROLE_USER" of "ROLE_ADMIN"
```

> **Waarom het prefix `ROLE_`?**  
> Spring Security verwacht dit prefix bij het gebruik van `.hasRole("ADMIN")`. Zonder prefix werkt de rolcontrole niet.

---

## Stap 2 — Wachtwoorden hashen met BCrypt

**Bestand:** `security/SecurityConfig.java`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Wachtwoorden worden **nooit in plaintext opgeslagen**. BCrypt is een hashing-algoritme dat:
- Een willekeurig **salt** toevoegt (elke hash is uniek, ook voor hetzelfde wachtwoord)
- Bewust **traag** is, zodat brute-force aanvallen lastig zijn

Bij het inloggen vergelijkt Spring het ingevoerde wachtwoord automatisch met de opgeslagen hash.

---

## Stap 3 — Gebruiker ophalen uit de database

**Bestand:** `security/UserDetailsServiceImpl.java`

```java
userRepository.findByEmail(email)
    .orElseThrow(() -> new UsernameNotFoundException(...));
```

Spring Security roept dit aan op het moment dat het een gebruiker nodig heeft voor verificatie. Het zoekt de gebruiker op via het e-mailadres (dat fungeert als gebruikersnaam).

---

## Stap 4 — Registreren en inloggen

**Bestand:** `service/AuthService.java`, `controller/AuthController.java`

### Registreren (`POST /api/auth/register`)

1. Ontvang `{ email, password }` — **geen rol in het request** (veiligheid: niemand kan zichzelf admin maken)
2. Hash het wachtwoord met BCrypt
3. Sla de gebruiker op met rol `USER`
4. Genereer een JWT token
5. Stuur het token terug

### Inloggen (`POST /api/auth/login`)

1. Ontvang `{ email, password }`
2. Gooi het door de `AuthenticationManager` — die vergelijkt automatisch met de database
3. Als het klopt: genereer een JWT token
4. Stuur het token terug

Bij een verkeerd wachtwoord gooit Spring een exception → HTTP 403.

---

## Stap 5 — JWT token aanmaken

**Bestand:** `security/JwtUtil.java`

Een JWT bestaat uit drie delen: `header.payload.signature`

Het token bevat:
```
{
  "sub": "gebruiker@email.nl",   ← het e-mailadres (subject)
  "role": "ROLE_ADMIN",          ← de rol
  "iat": 1234567890,             ← aangemaakt op (issued at)
  "exp": 1234654290              ← verloopdatum (24 uur later)
}
```

Het token wordt **ondertekend** met een geheime sleutel (HMAC-SHA384). Daardoor kan niemand het token aanpassen zonder dat de handtekening ongeldig wordt.

De geheime sleutel staat in `application.yaml` (en staat **niet** in Git):
```yaml
jwt:
  secret: 404E635266...   # lange hex-string
  expiration: 86400000    # 24 uur in milliseconden
```

---

## Stap 6 — Elke request valideren

**Bestand:** `security/JwtAuthenticationFilter.java`

Bij **elke** inkomende request loopt dit filter mee (extends `OncePerRequestFilter`):

```
Request binnenkomt
    ↓
Heeft het een "Authorization: Bearer <token>" header?
    ↓ Nee → gewoon doorgaan (SecurityConfig beslist dan of toegang mag)
    ↓ Ja
Token uitpakken en e-mailadres eruit halen
    ↓
Gebruiker ophalen uit database
    ↓
Is het token geldig én niet verlopen?
    ↓ Nee → niet inloggen, SecurityContext blijft leeg
    ↓ Ja
Gebruiker instellen in de SecurityContext
    ↓
Request verder afhandelen
```

De `SecurityContext` is een soort "geheugen voor deze request". Zodra de gebruiker erin staat, weet Spring Security wie de aanvrager is en welke rol hij heeft.

---

## Stap 7 — Toegangsregels per endpoint

**Bestand:** `security/SecurityConfig.java`

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()                        // Iedereen mag registreren/inloggen
    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()    // Producten bekijken: publiek
    .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")   // Aanmaken: alleen admin
    .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")    // Bewerken: alleen admin
    .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN") // Verwijderen: alleen admin
    .requestMatchers("/api/orders/**").authenticated()                  // Bestellingen: ingelogd
    .anyRequest().authenticated()                                       // Al het overige: ingelogd
)
```

Twee andere cruciale instellingen:

```java
.csrf(AbstractHttpConfigurer::disable)
// CSRF-beveiliging uit: niet nodig bij JWT omdat er geen cookies worden gebruikt.
// CSRF-aanvallen werken via cookies; JWT in een Authorization-header is immuun hiervoor.

.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
// Geen serversessies opslaan. De server "onthoudt" niemand tussen requests.
// Elke request moet zichzelf bewijzen via het JWT token.
```

---

## Stap 8 — Admin account bij opstarten

**Bestand:** `config/DataLoader.java`

Bij elke opstart controleert de applicatie of er al een admin-account bestaat. Zo niet, dan wordt er automatisch één aangemaakt met de gegevens uit `application.yaml`:

```yaml
admin:
  email: admin@webshop.nl
  password: admin123
```

Het wachtwoord wordt ook hier via BCrypt gehasht voordat het wordt opgeslagen.

---

## Samenvatting — Requestflow

```
Client stuurt request met "Authorization: Bearer eyJ..."
        ↓
JwtAuthenticationFilter pakt het token
        ↓
JwtUtil valideert handtekening + verloopdatum
        ↓
Gebruiker + rol worden in SecurityContext gezet
        ↓
SecurityConfig controleert of de rol toegang heeft tot dit endpoint
        ↓
Controller verwerkt het request (of: 403 Forbidden)
```

---

## Gebruikte technologieën

| Technologie | Versie | Doel |
|---|---|---|
| Spring Security 6 | 6.x | Authenticatie & autorisatie framework |
| JJWT | 0.12.6 | JWT aanmaken en valideren |
| BCrypt | (ingebouwd) | Wachtwoorden hashen |
| Spring Boot | 3.5.x | Autoconfiguratie en DI |
