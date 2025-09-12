erfa-backend

Quick start

1. Configure application properties (edit `src/main/resources/application.properties` or use environment variables):

- Mail settings:
  - spring.mail.host
  - spring.mail.port
  - spring.mail.username
  - spring.mail.password
  - spring.mail.properties.mail.smtp.auth=true
  - spring.mail.properties.mail.smtp.starttls.enable=true

- JWT settings:
  - jwt.secret (use a secure 256-bit random string)
  - jwt.expiration-ms (e.g., 3600000)

2. Build:

```powershell
cd \path\to\erfa-backend\erfa-backend
mvn -DskipTests clean install
```

3. Run:

```powershell
java -jar target\erfa-backend-0.0.1-SNAPSHOT.jar
```

Test endpoints

- Signup: POST /api/auth/signup
- Verify: GET /api/auth/verify?token=...
- Login: POST /api/auth/login -> returns JSON {"token": "..."}
- Test email: POST /api/auth/test-email {"to":"you@example.com","subject":"x","body":"y"}

Notes

- Replace secrets before production.
- JWT secret should be 32+ bytes for HS256.
