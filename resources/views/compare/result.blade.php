@extends('layouts.app')

@section('title', 'Excel Comparison Preview & Download')
@section('description', 'Review added, removed and changed rows from your Excel comparison, then download a JSON report.')
@section('keywords', 'excel comparison preview, spreadsheet diff report, download excel differences')
@section('canonical', url()->current())

@section('content')
    <section class="card">
        <h1>Comparison Preview</h1>
        <p class="lead">Result generated successfully. Review the differences below and download your report file.</p>
        <div class="kpi">
            <div class="kpi-item"><strong>Added</strong><br>{{ $result['summary']['added'] }}</div>
            <div class="kpi-item"><strong>Removed</strong><br>{{ $result['summary']['removed'] }}</div>
            <div class="kpi-item"><strong>Changed</strong><br>{{ $result['summary']['changed'] }}</div>
        </div>
        <div style="margin-top: 10px;">
            <a class="btn" href="{{ route('compare.download', ['token' => $downloadToken]) }}">Download CSV Report</a>
            <a class="btn btn-secondary" href="{{ route('compare.index') }}">Compare Another File Pair</a>
        </div>
    </section>

    @if (!empty($result['added']))
        <section class="card">
            <h2>Added Rows</h2>
            <div style="overflow:auto;">
                <table>
                    <thead>
                    <tr>
                        <th>Key</th>
                        <th>Row</th>
                    </tr>
                    </thead>
                    <tbody>
                    @foreach ($result['added'] as $row)
                        <tr>
                            <td>{{ $row['key'] }}</td>
                            <td>{{ json_encode($row['row'], JSON_UNESCAPED_UNICODE) }}</td>
                        </tr>
                    @endforeach
                    </tbody>
                </table>
            </div>
        </section>
    @endif

    @if (!empty($result['removed']))
        <section class="card">
            <h2>Removed Rows</h2>
            <div style="overflow:auto;">
                <table>
                    <thead>
                    <tr>
                        <th>Key</th>
                        <th>Row</th>
                    </tr>
                    </thead>
                    <tbody>
                    @foreach ($result['removed'] as $row)
                        <tr>
                            <td>{{ $row['key'] }}</td>
                            <td>{{ json_encode($row['row'], JSON_UNESCAPED_UNICODE) }}</td>
                        </tr>
                    @endforeach
                    </tbody>
                </table>
            </div>
        </section>
    @endif

    @if (!empty($result['changed']))
        <section class="card">
            <h2>Changed Rows</h2>
            @foreach ($result['changed'] as $row)
                <article style="margin-bottom: 14px;">
                    <strong>Key: {{ $row['key'] }}</strong>
                    <ul>
                        @foreach ($row['diff'] as $field => $change)
                            <li>{{ $field }}: "{{ $change['old'] }}" -> "{{ $change['new'] }}"</li>
                        @endforeach
                    </ul>
                </article>
            @endforeach
        </section>
    @endif

    @if (empty($result['added']) && empty($result['removed']) && empty($result['changed']))
        <section class="card">
            <p>No differences found between the two Excel files.</p>
        </section>
    @endif
@endsection
