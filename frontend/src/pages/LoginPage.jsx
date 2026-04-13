import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [phone, setPhone] = useState("");
  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [sentOtpValue, setSentOtpValue] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const requestOtp = async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.post("/auth/request-otp", { phone });
      setOtpSent(true);
      setSentOtpValue(data.otp);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to send OTP");
    } finally {
      setLoading(false);
    }
  };

  const verifyOtp = async () => {
    setLoading(true);
    setError("");

    try {
      const { data } = await api.post("/auth/verify-otp", { phone, otp });
      login(data);

      if (data.broker?.isOnboarded) {
        navigate("/");
      } else {
        navigate("/onboarding");
      }
    } catch (err) {
      setError(err.response?.data?.message || "OTP verification failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <section className="auth-card">
        <h2>Login / Onboarding</h2>
        <p>Use mobile OTP to access your broker dashboard.</p>

        <label>Mobile Number</label>
        <input value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="Enter mobile" />

        {!otpSent ? (
          <button className="btn" onClick={requestOtp} disabled={loading || phone.length < 8}>
            {loading ? "Sending..." : "Send OTP"}
          </button>
        ) : (
          <>
            <label>OTP</label>
            <input value={otp} onChange={(e) => setOtp(e.target.value)} placeholder="Enter 6 digit OTP" />
            <button className="btn" onClick={verifyOtp} disabled={loading || otp.length !== 6}>
              {loading ? "Verifying..." : "Verify OTP"}
            </button>
            <p className="helper">Demo OTP: {sentOtpValue}</p>
          </>
        )}

        {error && <p className="error-text">{error}</p>}
      </section>
    </div>
  );
}
