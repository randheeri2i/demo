@extends('layouts.app')

@section('title', 'Page Not Found')
@section('description', 'The page you are looking for does not exist.')

@section('content')
    <section class="card">
        <h1>404 - Page Not Found</h1>
        <p class="muted">The page you requested does not exist.</p>
        <a class="btn" href="{{ url('/') }}">Back to Home</a>
    </section>
@endsection
