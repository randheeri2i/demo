# demo

Compare two Excel files using PHP.

## Setup

```bash
composer install
```

## Usage

```bash
php compare_excel.php <file1.xlsx> <file2.xlsx> [sheet] [key_column]
```

Examples:

```bash
php compare_excel.php old.xlsx new.xlsx
php compare_excel.php old.xlsx new.xlsx Sheet1 A
php compare_excel.php old.xlsx new.xlsx Sheet1 "Employee ID"
```

Arguments:

- `file1.xlsx`: Baseline Excel file.
- `file2.xlsx`: New Excel file to compare with baseline.
- `sheet` (optional): Sheet name to compare. Defaults to each file's active sheet.
- `key_column` (optional): Unique key column (column letter like `A` or header name). Defaults to `A`.

Output includes:

- Added rows
- Removed rows
- Changed rows (field-by-field differences)
