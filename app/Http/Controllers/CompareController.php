<?php

namespace App\Http\Controllers;

use App\Services\ExcelComparisonService;
use Illuminate\Http\Request;
use Illuminate\Http\RedirectResponse;
use Illuminate\View\View;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use RuntimeException;

class CompareController extends Controller
{
    public function index(): View
    {
        return view('dashboard');
    }

    public function compare(Request $request, ExcelComparisonService $service): View|RedirectResponse
    {
        $validated = $request->validate([
            'baseline_file' => ['required', 'file', 'mimes:xlsx,xls,csv'],
            'updated_file' => ['required', 'file', 'mimes:xlsx,xls,csv'],
            'sheet_name' => ['nullable', 'string', 'max:120'],
            'key_column' => ['required', 'string', 'max:120'],
        ]);

        $fileOnePath = $request->file('baseline_file')->store('uploads', 'local');
        $fileTwoPath = $request->file('updated_file')->store('uploads', 'local');

        $fullPathOne = Storage::disk('local')->path($fileOnePath);
        $fullPathTwo = Storage::disk('local')->path($fileTwoPath);

        try {
            $result = $service->compare(
                $fullPathOne,
                $fullPathTwo,
                $validated['sheet_name'] ?? null,
                $validated['key_column']
            );
        } catch (RuntimeException $exception) {
            return back()
                ->withInput($request->except(['baseline_file', 'updated_file']))
                ->withErrors(['compare' => $exception->getMessage()]);
        }

        $downloadToken = (string) Str::uuid();
        $csvPath = "downloads/{$downloadToken}.csv";
        Storage::disk('local')->put($csvPath, $this->buildCsvReport($result));

        return view('compare.result', [
            'result' => $result,
            'downloadToken' => $downloadToken,
        ]);
    }

    public function download(string $token)
    {
        $path = "downloads/{$token}.csv";
        abort_unless(Storage::disk('local')->exists($path), 404);

        return Storage::disk('local')->download($path, "excel-comparison-{$token}.csv");
    }

    /**
     * @param array{
     *   keyColumn:string,
     *   summary:array{added:int,removed:int,changed:int},
     *   added:array<int,array{key:string,row:array<string,string>}>,
     *   removed:array<int,array{key:string,row:array<string,string>}>,
     *   changed:array<int,array{key:string,diff:array<string,array{old:string,new:string}>}>
     * } $result
     */
    private function buildCsvReport(array $result): string
    {
        $handle = fopen('php://temp', 'w+');
        if ($handle === false) {
            return '';
        }

        fputcsv($handle, ['section', 'key', 'field', 'old_value', 'new_value', 'row_data_json']);

        foreach ($result['added'] as $item) {
            fputcsv($handle, ['added', $item['key'], '', '', '', json_encode($item['row'], JSON_UNESCAPED_UNICODE)]);
        }

        foreach ($result['removed'] as $item) {
            fputcsv($handle, ['removed', $item['key'], '', '', '', json_encode($item['row'], JSON_UNESCAPED_UNICODE)]);
        }

        foreach ($result['changed'] as $item) {
            foreach ($item['diff'] as $field => $values) {
                fputcsv($handle, ['changed', $item['key'], $field, $values['old'], $values['new'], '']);
            }
        }

        rewind($handle);
        $csv = stream_get_contents($handle);
        fclose($handle);

        return $csv !== false ? $csv : '';
    }
}
