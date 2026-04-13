@extends('layouts.app')

@section('title', 'Login | Excel Compare Pro')
@section('description', 'Login to securely compare two Excel files and download difference reports.')

@section('content')
    <section class="card">
        <h1>Login</h1>
        <p class="muted">Sign in to upload files, preview differences, and download reports.</p>

        <form action="{{ route('login.attempt') }}" method="POST">
            @csrf

            <div class="field">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="{{ old('email') }}" required>
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required>
            </div>

            <button class="btn primary" type="submit">Login</button>
            <a class="btn link" href="{{ route('register') }}">Create account</a>
        </form>
    </section>
@endsection
