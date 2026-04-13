@extends('layouts.app')

@section('title', 'Something Went Wrong | Excel Compare Pro')
@section('description', 'An unexpected error occurred while processing your request.')

@section('content')
    <section class="card">
        <h1>Something went wrong</h1>
        <p>Please go back and try again. If the issue continues, refresh the page.</p>
        <a class="btn" href="{{ route('compare.index') }}">Back to Dashboard</a>
    </section>
@endsection
