<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>@yield('title', 'Excel Compare Pro | Compare Excel Online')</title>
    <meta name="description" content="@yield('description', 'Compare two Excel files, preview differences, and download a report with secure login.')">
    <meta name="keywords" content="@yield('keywords', 'excel compare, xlsx diff, spreadsheet compare, excel preview, download report')">
    <meta name="robots" content="index,follow">
    <link rel="canonical" href="@yield('canonical', url()->current())">
    <meta property="og:type" content="website">
    <meta property="og:title" content="@yield('title', 'Excel Compare Pro | Compare Excel Online')">
    <meta property="og:description" content="@yield('description', 'Compare two Excel files, preview differences, and download a report with secure login.')">
    <meta property="og:url" content="@yield('canonical', url()->current())">
    <link rel="stylesheet" href="{{ asset('css/app.css') }}">
</head>
<body>
    <header class="topbar">
        <div class="container">
            <a class="brand" href="{{ route('compare.index') }}">
                <span>Excel Compare Pro</span>
            </a>
            <nav class="actions">
                @auth
                    <a class="btn outline" href="{{ route('compare.index') }}">Dashboard</a>
                    <form method="POST" action="{{ route('logout') }}">
                        @csrf
                        <button class="btn danger" type="submit">Logout</button>
                    </form>
                @else
                    <a class="btn link" href="{{ route('login') }}">Login</a>
                    <a class="btn primary" href="{{ route('register') }}">Create account</a>
                @endauth
            </nav>
        </div>
    </header>

    <main class="main">
        <div class="container">
            @if ($errors->any())
                <div class="alert error">
                    <strong>Please fix the following:</strong>
                    <ul>
                        @foreach ($errors->all() as $error)
                            <li>{{ $error }}</li>
                        @endforeach
                    </ul>
                </div>
            @endif

            @if (session('status'))
                <div class="alert success">{{ session('status') }}</div>
            @endif

            @yield('content')
        </div>
    </main>
</body>
</html>
