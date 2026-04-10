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

## Installation

```bash
composer install
cp .env.example .env
php artisan key:generate
touch database/database.sqlite
php artisan migrate
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
