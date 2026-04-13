# PinMyHome

PinMyHome is a broker-first real estate workflow app built with:

- **Frontend:** React (Vite)
- **Backend API:** Node.js + Express
- **Databases:** PostgreSQL + MongoDB

Theme colors are inspired by the provided logo (teal, deep blue, warm orange accents).

## Features implemented

### Authentication & Onboarding
- Mobile OTP request and verification
- Broker account registration with:
  - Full Name
  - Email
  - City
  - Agency Name
- JWT-based session auth

### Broker Dashboard
Navigation:
- Home
- Listings
- Deals
- Profile

### Listings
- Add Property (multi-step flow):
  1. Basic Details: Title, BHK, Area (Sqft), Expected Price
  2. Location: Address + map coordinates (lat/lng placeholder)
  3. Upload Photos: unlimited photo URLs
- My Listings view
- Hot Listings with search & filter inputs
- Property detail page with image carousel-like preview
- Property details include:
  - Price
  - BHK
  - Platform Margin
  - Area

### Deals Tracking
- My Deals list with:
  - Property
  - Buyer Name
  - Status of visit
- Add new deal
- Update deal status

### Visits
- Schedule visit from property detail page
- Choose date and time
- Confirm visit

### Dashboard Activity
- Active Deals count
- Visits Today count

---

## Project structure

```bash
/workspace
  ├─ frontend   # React app
  └─ backend    # Node/Express API
```

---

## Backend setup (`/backend`)

### 1) Environment

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

`backend/.env.example`:

```env
PORT=5000
CLIENT_URL=http://localhost:5173
JWT_SECRET=replace_with_strong_secret
JWT_EXPIRES_IN=7d

POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=pinmyhome

MONGODB_URI=mongodb://localhost:27017/pinmyhome
```

### 2) Install and run

```bash
npm install
npm run dev
```

API base URL: `http://localhost:5000/api`

Health check: `GET /api/health`

---

## Frontend setup (`/frontend`)

### 1) Environment

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

`frontend/.env.example`:

```env
VITE_API_BASE_URL=http://localhost:5000/api
```

### 2) Install and run

```bash
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

---

## Database usage

### PostgreSQL
Used for relational broker/auth entities:
- `brokers`
- `otp_sessions`

These tables are initialized automatically on backend startup.

### MongoDB
Used for document-heavy workflow entities:
- `properties`
- `deals`
- `visits`

---

## Notes

- OTP is returned in API response for development/demo.
- For production, plug an SMS provider (e.g., MSG91/Twilio) into `requestOtp`.
- Photo upload is currently URL-based; you can extend to S3/Cloudinary direct upload.
