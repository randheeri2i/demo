import { useAuth } from "../context/AuthContext";

export default function ProfilePage() {
  const { broker } = useAuth();

  return (
    <div className="card">
      <h2>Profile</h2>
      <div className="profile-grid">
        <div>
          <span>Full Name</span>
          <strong>{broker?.fullName || "-"}</strong>
        </div>
        <div>
          <span>Phone</span>
          <strong>{broker?.phone || "-"}</strong>
        </div>
        <div>
          <span>Email</span>
          <strong>{broker?.email || "-"}</strong>
        </div>
        <div>
          <span>City</span>
          <strong>{broker?.city || "-"}</strong>
        </div>
        <div>
          <span>Agency</span>
          <strong>{broker?.agencyName || "-"}</strong>
        </div>
      </div>
    </div>
  );
}
