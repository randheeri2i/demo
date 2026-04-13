import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const links = [
  { to: "/", label: "Home" },
  { to: "/listings", label: "Listings" },
  { to: "/deals", label: "Deals" },
  { to: "/profile", label: "Profile" },
];

export default function DashboardLayout() {
  const { logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-pin">P</div>
          <div>
            <h1>PinMyHome</h1>
            <p>Broker workspace</p>
          </div>
        </div>
        <button className="btn btn-secondary" onClick={logout}>
          Logout
        </button>
      </header>

      <nav className="navbar">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.to === "/"}
            className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
          >
            {link.label}
          </NavLink>
        ))}
      </nav>

      <main className="page-content">
        <Outlet />
      </main>
    </div>
  );
}
