<?php

declare(strict_types=1);

namespace App\Services;

use PhpOffice\PhpSpreadsheet\Cell\Coordinate;
use PhpOffice\PhpSpreadsheet\IOFactory;
use RuntimeException;
use Throwable;

class ExcelComparisonService
{
    /**
     * @return array{
     *   keyColumn:string,
     *   headers:array<int,string>,
     *   summary:array{added:int,removed:int,changed:int},
     *   added:array<int,array{key:string,row:array<string,string>}>,
     *   removed:array<int,array{key:string,row:array<string,string>}>,
     *   changed:array<int,array{key:string,diff:array<string,array{old:string,new:string}>}>
     * }
     */
    public function compare(
        string $oldFilePath,
        string $newFilePath,
        ?string $sheetName,
        string $keyColumnInput
    ): array {
        $rows1 = $this->loadRows($oldFilePath, $sheetName);
        $rows2 = $this->loadRows($newFilePath, $sheetName);

        $headers = $this->mergeHeaders($rows1, $rows2);
        if ($headers === []) {
            return [
                'keyColumn' => $keyColumnInput,
                'headers' => [],
                'summary' => ['added' => 0, 'removed' => 0, 'changed' => 0],
                'added' => [],
                'removed' => [],
                'changed' => [],
            ];
        }

        $keyColumn = $this->resolveKeyColumn($headers, $keyColumnInput);
        if ($keyColumn === null) {
            throw new RuntimeException(
                "Key column '{$keyColumnInput}' not found. Available columns: " . implode(', ', $headers)
            );
        }

        $index1 = $this->indexByKey($rows1, $keyColumn);
        $index2 = $this->indexByKey($rows2, $keyColumn);

        $allKeys = array_unique(array_merge(array_keys($index1), array_keys($index2)));
        sort($allKeys, SORT_NATURAL);

        $added = [];
        $removed = [];
        $changed = [];

        foreach ($allKeys as $key) {
            $in1 = array_key_exists($key, $index1);
            $in2 = array_key_exists($key, $index2);

            if (!$in1 && $in2) {
                $added[] = ['key' => $key, 'row' => $index2[$key]];
                continue;
            }

            if ($in1 && !$in2) {
                $removed[] = ['key' => $key, 'row' => $index1[$key]];
                continue;
            }

            $diff = $this->compareRows($index1[$key], $index2[$key], $headers, $keyColumn);
            if ($diff !== []) {
                $changed[] = ['key' => $key, 'diff' => $diff];
            }
        }

        return [
            'keyColumn' => $keyColumn,
            'headers' => $headers,
            'summary' => [
                'added' => count($added),
                'removed' => count($removed),
                'changed' => count($changed),
            ],
            'added' => $added,
            'removed' => $removed,
            'changed' => $changed,
        ];
    }

    /**
     * @return array<int, array<string, string>>
     */
    private function loadRows(string $filePath, ?string $sheetName): array
    {
        try {
            $reader = IOFactory::createReaderForFile($filePath);
            $reader->setReadDataOnly(true);
            $spreadsheet = $reader->load($filePath);
        } catch (Throwable $throwable) {
            throw new RuntimeException("Failed reading Excel file '{$filePath}': {$throwable->getMessage()}");
        }

        $sheet = $sheetName ? $spreadsheet->getSheetByName($sheetName) : $spreadsheet->getActiveSheet();
        if ($sheet === null) {
            throw new RuntimeException("Sheet '{$sheetName}' not found in {$filePath}.");
        }

        $highestColumn = $sheet->getHighestDataColumn();
        $highestColumnIndex = Coordinate::columnIndexFromString($highestColumn);
        $highestRow = $sheet->getHighestDataRow();

        if ($highestColumnIndex < 1 || $highestRow < 1) {
            return [];
        }

        $rawHeaders = [];
        for ($col = 1; $col <= $highestColumnIndex; $col++) {
            $header = trim((string) $sheet->getCell([$col, 1])->getFormattedValue());
            $rawHeaders[] = $header === '' ? 'Column' . $col : $header;
        }
        $headers = $this->makeUniqueHeaders($rawHeaders);

        $rows = [];
        for ($row = 2; $row <= $highestRow; $row++) {
            $record = [];
            $hasValue = false;
            for ($col = 1; $col <= $highestColumnIndex; $col++) {
                $value = trim((string) $sheet->getCell([$col, $row])->getFormattedValue());
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
     * @param array<int, string> $headers
     * @return array<int, string>
     */
    private function makeUniqueHeaders(array $headers): array
    {
        $counts = [];
        $result = [];

        foreach ($headers as $header) {
            if (!array_key_exists($header, $counts)) {
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
    private function mergeHeaders(array $rows1, array $rows2): array
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
    private function resolveKeyColumn(array $headers, string $input): ?string
    {
        $trimmed = trim($input);
        if ($trimmed === '') {
            return null;
        }

        if (ctype_alpha($trimmed)) {
            $index = Coordinate::columnIndexFromString(strtoupper($trimmed)) - 1;
            if (isset($headers[$index])) {
                return $headers[$index];
            }
        }

        foreach ($headers as $header) {
            if (strcasecmp($header, $trimmed) === 0) {
                return $header;
            }
        }

        return null;
    }

    /**
     * @param array<int, array<string, string>> $rows
     * @return array<string, array<string, string>>
     */
    private function indexByKey(array $rows, string $keyColumn): array
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
     * @return array<string, array{old:string,new:string}>
     */
    private function compareRows(array $row1, array $row2, array $headers, string $keyColumn): array
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
}
