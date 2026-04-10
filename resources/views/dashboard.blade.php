@extends('layouts.app')

@section('title', 'Excel Compare Dashboard')
@section('description', 'Compare two Excel files online, preview differences instantly, and download a CSV report.')

@section('content')
    <section class="card hero">
        <h1>Compare two Excel files in seconds</h1>
        <p class="muted">Upload baseline and updated spreadsheets, preview row-level differences, then download a detailed report.</p>
    </section>

    <section class="card">
        <h2>Upload spreadsheets</h2>
        <form method="POST" action="{{ route('compare.run') }}" enctype="multipart/form-data">
            @csrf

            <div class="grid two">
                <div class="field">
                    <label for="baseline_file">Baseline file</label>
                    <input id="baseline_file" type="file" name="baseline_file" accept=".xlsx,.xls,.csv" required>
                    <small>Older/original data file.</small>
                </div>

                <div class="field">
                    <label for="updated_file">Updated file</label>
                    <input id="updated_file" type="file" name="updated_file" accept=".xlsx,.xls,.csv" required>
                    <small>Newer file to compare against baseline.</small>
                </div>
            </div>

            <div class="grid two">
                <div class="field">
                    <label for="sheet_name">Sheet name (optional)</label>
                    <input id="sheet_name" type="text" name="sheet_name" value="{{ old('sheet_name') }}" placeholder="Sheet1">
                    <small>Leave blank to use each file's active sheet.</small>
                </div>

                <div class="field">
                    <label for="key_column">Key column</label>
                    <input id="key_column" type="text" name="key_column" value="{{ old('key_column', 'A') }}" required placeholder="A or Employee ID">
                    <small>Use column letter (A, B, C...) or exact header name.</small>
                </div>
            </div>

            <button type="submit" class="btn primary">Preview comparison</button>
        </form>
    </section>
@endsection
