<?php

declare(strict_types=1);

require __DIR__ . '/vendor/autoload.php';

use PhpOffice\PhpSpreadsheet\Cell\Coordinate;
use PhpOffice\PhpSpreadsheet\IOFactory;
use PhpOffice\PhpSpreadsheet\Worksheet\Worksheet;

if ($argc < 3) {
    fwrite(STDERR, "Usage: php compare_excel.php <file1.xlsx> <file2.xlsx> [sheet] [key_column]\n");
    fwrite(STDERR, "Example: php compare_excel.php old.xlsx new.xlsx Sheet1 A\n");
    exit(1);
}

$file1 = $argv[1];
$file2 = $argv[2];
$sheetName = $argv[3] ?? null;
$keyColumnInput = $argv[4] ?? 'A';

if (!is_file($file1)) {
    fwrite(STDERR, "File not found: {$file1}\n");
    exit(1);
}

if (!is_file($file2)) {
    fwrite(STDERR, "File not found: {$file2}\n");
    exit(1);
}

try {
    $rows1 = loadRows($file1, $sheetName);
    $rows2 = loadRows($file2, $sheetName);
} catch (Throwable $e) {
    fwrite(STDERR, "Error reading files: {$e->getMessage()}\n");
    exit(1);
}

if (empty($rows1) && empty($rows2)) {
    echo "Both sheets are empty.\n";
    exit(0);
}

$headers = mergeHeaders($rows1, $rows2);
$keyColumnName = resolveKeyColumn($headers, $keyColumnInput);

if ($keyColumnName === null) {
    fwrite(STDERR, "Key column '{$keyColumnInput}' not found.\n");
    fwrite(STDERR, "Available columns: " . implode(', ', $headers) . "\n");
    exit(1);
}

$index1 = indexByKey($rows1, $keyColumnName);
$index2 = indexByKey($rows2, $keyColumnName);

$added = [];
$removed = [];
$changed = [];

$allKeys = array_unique(array_merge(array_keys($index1), array_keys($index2)));
sort($allKeys, SORT_NATURAL);

foreach ($allKeys as $key) {
    $in1 = array_key_exists($key, $index1);
    $in2 = array_key_exists($key, $index2);

    if (!$in1 && $in2) {
        $added[] = [$key, $index2[$key]];
        continue;
    }

    if ($in1 && !$in2) {
        $removed[] = [$key, $index1[$key]];
        continue;
    }

    $diff = compareRows($index1[$key], $index2[$key], $headers, $keyColumnName);
    if (!empty($diff)) {
        $changed[] = [$key, $diff];
    }
}

printSummary($file1, $file2, $sheetName, $keyColumnName, $added, $removed, $changed);

/**
 * @return array<int, array<string, string>>
 */
function loadRows(string $filePath, ?string $sheetName): array
{
    $reader = IOFactory::createReaderForFile($filePath);
    $reader->setReadDataOnly(true);
    $spreadsheet = $reader->load($filePath);

    $sheet = $sheetName !== null
        ? getSheetByNameOrFail($spreadsheet->getAllSheets(), $sheetName)
        : $spreadsheet->getActiveSheet();

    $highestColumn = $sheet->getHighestDataColumn();
    $highestColumnIndex = Coordinate::columnIndexFromString($highestColumn);
    $highestRow = $sheet->getHighestDataRow();

    if ($highestColumnIndex < 1 || $highestRow < 1) {
        return [];
    }

    $rawHeaders = [];
    for ($col = 1; $col <= $highestColumnIndex; $col++) {
        $header = trim((string) $sheet->getCellByColumnAndRow($col, 1)->getFormattedValue());
        if ($header === '') {
            $header = 'Column' . $col;
        }
        $rawHeaders[] = $header;
    }

    $headers = makeUniqueHeaders($rawHeaders);
    $rows = [];

    for ($row = 2; $row <= $highestRow; $row++) {
        $record = [];
        $hasValue = false;

        for ($col = 1; $col <= $highestColumnIndex; $col++) {
            $value = trim((string) $sheet->getCellByColumnAndRow($col, $row)->getFormattedValue());
            if ($value !== '') {
                $hasValue = true;
            }
            $record[$headers[$col - 1]] = $value;
        }

        if ($hasValue) {
            $rows[] = $record;
        }
    }

    return $rows;
}

/**
 * @param array<int, Worksheet> $sheets
 */
function getSheetByNameOrFail(array $sheets, string $sheetName): Worksheet
{
    foreach ($sheets as $sheet) {
        if ($sheet->getTitle() === $sheetName) {
            return $sheet;
        }
    }

    throw new RuntimeException("Sheet '{$sheetName}' not found.");
}

/**
 * @param array<int, string> $headers
 * @return array<int, string>
 */
function makeUniqueHeaders(array $headers): array
{
    $counts = [];
    $result = [];

    foreach ($headers as $header) {
        if (!isset($counts[$header])) {
            $counts[$header] = 0;
            $result[] = $header;
            continue;
        }

        $counts[$header]++;
        $result[] = $header . '_' . $counts[$header];
    }

    return $result;
}

/**
 * @param array<int, array<string, string>> $rows1
 * @param array<int, array<string, string>> $rows2
 * @return array<int, string>
 */
function mergeHeaders(array $rows1, array $rows2): array
{
    $headers = [];
    $append = static function (array $rows) use (&$headers): void {
        foreach ($rows as $row) {
            foreach (array_keys($row) as $key) {
                if (!in_array($key, $headers, true)) {
                    $headers[] = $key;
                }
            }
        }
    };

    $append($rows1);
    $append($rows2);

    return $headers;
}

/**
 * @param array<int, string> $headers
 */
function resolveKeyColumn(array $headers, string $input): ?string
{
    $trimmedInput = trim($input);
    if ($trimmedInput === '') {
        return null;
    }

    if (ctype_alpha($trimmedInput)) {
        $colIndex = Coordinate::columnIndexFromString(strtoupper($trimmedInput)) - 1;
        if (isset($headers[$colIndex])) {
            return $headers[$colIndex];
        }
    }

    foreach ($headers as $header) {
        if (strcasecmp($header, $trimmedInput) === 0) {
            return $header;
        }
    }

    return null;
}

/**
 * @param array<int, array<string, string>> $rows
 * @return array<string, array<string, string>>
 */
function indexByKey(array $rows, string $keyColumn): array
{
    $indexed = [];
    $dupCount = [];

    foreach ($rows as $row) {
        $baseKey = $row[$keyColumn] ?? '';
        $baseKey = $baseKey === '' ? '__EMPTY_KEY__' : $baseKey;

        $suffix = $dupCount[$baseKey] ?? 0;
        $uniqueKey = $suffix === 0 ? $baseKey : "{$baseKey}#{$suffix}";

        $dupCount[$baseKey] = $suffix + 1;
        $indexed[$uniqueKey] = $row;
    }

    return $indexed;
}

/**
 * @param array<string, string> $row1
 * @param array<string, string> $row2
 * @param array<int, string> $headers
 * @return array<string, array{old: string, new: string}>
 */
function compareRows(array $row1, array $row2, array $headers, string $keyColumn): array
{
    $diff = [];

    foreach ($headers as $header) {
        if ($header === $keyColumn) {
            continue;
        }

        $old = $row1[$header] ?? '';
        $new = $row2[$header] ?? '';

        if ($old !== $new) {
            $diff[$header] = ['old' => $old, 'new' => $new];
        }
    }

    return $diff;
}

/**
 * @param array<int, array{0: string, 1: array<string, string>}> $added
 * @param array<int, array{0: string, 1: array<string, string>}> $removed
 * @param array<int, array{0: string, 1: array<string, array{old: string, new: string}>}> $changed
 */
function printSummary(
    string $file1,
    string $file2,
    ?string $sheetName,
    string $keyColumn,
    array $added,
    array $removed,
    array $changed
): void {
    echo "Comparing:\n";
    echo " - File 1: {$file1}\n";
    echo " - File 2: {$file2}\n";
    echo " - Sheet : " . ($sheetName ?? '(active sheet)') . "\n";
    echo " - Key   : {$keyColumn}\n\n";

    echo "Result summary:\n";
    echo " - Added rows   : " . count($added) . "\n";
    echo " - Removed rows : " . count($removed) . "\n";
    echo " - Changed rows : " . count($changed) . "\n\n";

    if (!empty($added)) {
        echo "Added rows:\n";
        foreach ($added as [$key, $row]) {
            echo " + {$key}: " . json_encode($row, JSON_UNESCAPED_SLASHES) . "\n";
        }
        echo "\n";
    }

    if (!empty($removed)) {
        echo "Removed rows:\n";
        foreach ($removed as [$key, $row]) {
            echo " - {$key}: " . json_encode($row, JSON_UNESCAPED_SLASHES) . "\n";
        }
        echo "\n";
    }

    if (!empty($changed)) {
        echo "Changed rows:\n";
        foreach ($changed as [$key, $fields]) {
            echo " * {$key}\n";
            foreach ($fields as $field => $values) {
                echo "    - {$field}: '{$values['old']}' => '{$values['new']}'\n";
            }
        }
        echo "\n";
    }

    if (empty($added) && empty($removed) && empty($changed)) {
        echo "No differences found.\n";
    }
}
