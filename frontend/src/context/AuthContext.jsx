import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("pinmyhome_token"));
  const [broker, setBroker] = useState(null);
  const [loading, setLoading] = useState(Boolean(token));

  const refreshMe = async () => {
    if (!token) {
      setBroker(null);
      setLoading(false);
      return;
    }

    try {
      const { data } = await api.get("/auth/me");
      setBroker(data.broker);
    } catch (_error) {
      localStorage.removeItem("pinmyhome_token");
      setToken(null);
      setBroker(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshMe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const login = ({ token: nextToken, broker: nextBroker }) => {
    localStorage.setItem("pinmyhome_token", nextToken);
    setToken(nextToken);
    setBroker(nextBroker);
  };

  const logout = () => {
    localStorage.removeItem("pinmyhome_token");
    setToken(null);
    setBroker(null);
  };

  const value = useMemo(
    () => ({
      token,
      broker,
      loading,
      login,
      logout,
      setBroker,
      refreshMe,
      isAuthenticated: Boolean(token),
    }),
    [token, broker, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}
