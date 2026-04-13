@extends('layouts.app')

@section('title', 'Register | Excel Compare Pro')
@section('description', 'Create your account to compare two Excel files and download a detailed report.')

@section('content')
    <section class="card">
        <h1>Create account</h1>
        <p class="muted">Start comparing Excel files in minutes.</p>

        <form method="POST" action="{{ route('register.store') }}">
            @csrf
            <div class="field">
                <label for="name">Name</label>
                <input id="name" type="text" name="name" value="{{ old('name') }}" required>
            </div>

            <div class="field">
                <label for="email">Email</label>
                <input id="email" type="email" name="email" value="{{ old('email') }}" required>
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input id="password" type="password" name="password" required>
                <small>Use a strong password with at least 8 characters.</small>
            </div>

            <div class="field">
                <label for="password_confirmation">Confirm Password</label>
                <input id="password_confirmation" type="password" name="password_confirmation" required>
            </div>

            <button class="btn primary" type="submit">Create Account</button>
        </form>
    </section>
@endsection
