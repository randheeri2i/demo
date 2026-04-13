import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function OnboardingPage() {
  const navigate = useNavigate();
  const { broker, setBroker } = useAuth();
  const [form, setForm] = useState({
    fullName: broker?.fullName || "",
    email: broker?.email || "",
    city: broker?.city || "",
    agencyName: broker?.agencyName || "",
  });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const onChange = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");

    try {
      const { data } = await api.post("/auth/onboarding", form);
      setBroker(data.broker);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Unable to complete onboarding");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={onSubmit}>
        <h2>Broker Registration</h2>
        <p>Complete your profile with basic details.</p>

        <label>Full Name</label>
        <input value={form.fullName} onChange={(e) => onChange("fullName", e.target.value)} required />

        <label>Email</label>
        <input type="email" value={form.email} onChange={(e) => onChange("email", e.target.value)} required />

        <label>City</label>
        <input value={form.city} onChange={(e) => onChange("city", e.target.value)} required />

        <label>Agency Name</label>
        <input value={form.agencyName} onChange={(e) => onChange("agencyName", e.target.value)} required />

        <button className="btn" disabled={saving} type="submit">
          {saving ? "Saving..." : "Complete Registration"}
        </button>

        {error && <p className="error-text">{error}</p>}
      </form>
    </div>
  );
}
