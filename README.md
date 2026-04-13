# Excel Compare Pro (Laravel)

A user-friendly Laravel application to:
- Login/Register securely
- Upload and compare two Excel files (`.xlsx`, `.xls`, `.csv`)
- Preview added/removed/changed rows
- Download the comparison result as a CSV report
- Serve SEO-friendly pages with proper meta tags

## Features

- **Authentication**: Register, login, logout
- **Excel Comparison**:
  - choose optional sheet name
  - choose key column by letter (e.g. `A`) or header name
  - detect added / removed / changed rows
- **Preview UI**:
  - summary tiles
  - detailed difference tables
  - readable field-level change lists
- **Download**:
  - downloadable comparison report (`.csv`)
- **SEO-focused UI**:
  - meaningful page titles
  - meta description and keywords
  - canonical URL
  - Open Graph tags

## Requirements

- PHP 8.2+
- Composer

## Installation (MySQL)

```bash
composer install
cp .env.example .env
php artisan key:generate
# configure MySQL credentials in .env
php artisan migrate
```

Create a MySQL database before running migrations (example):

```sql
CREATE DATABASE excel_compare_pro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Default `.env` database values:

```dotenv
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=excel_compare_pro
DB_USERNAME=root
DB_PASSWORD=
```

## Run

```bash
php artisan serve
```

Visit:

- `http://127.0.0.1:8000/login`

## Usage Flow

1. Register a new account (or login)
2. Go to dashboard
3. Upload baseline and updated Excel files
4. Optionally set sheet name and key column
5. Click **Preview Comparison**
6. Review diffs and click **Download CSV Report**
