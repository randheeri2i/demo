import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

const initialForm = {
  title: "",
  bhk: "",
  areaSqft: "",
  expectedPrice: "",
  platformMargin: "",
  address: "",
  latitude: "",
  longitude: "",
  areaTag: "Noida",
  photosText: "",
};

export default function AddPropertyPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState(initialForm);

  const setValue = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const submitProperty = async () => {
    setSaving(true);
    setError("");

    try {
      const photos = form.photosText
        .split("\n")
        .map((line) => line.trim())
        .filter(Boolean);

      await api.post("/properties", {
        title: form.title,
        bhk: Number(form.bhk),
        areaSqft: Number(form.areaSqft),
        expectedPrice: Number(form.expectedPrice),
        platformMargin: Number(form.platformMargin || 0),
        address: form.address,
        latitude: form.latitude ? Number(form.latitude) : undefined,
        longitude: form.longitude ? Number(form.longitude) : undefined,
        areaTag: form.areaTag,
        photos,
      });

      navigate("/listings");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to add property");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="card">
      <h2>Add Property</h2>
      <p className="muted">Step {step} of 3</p>

      {step === 1 && (
        <div className="form-grid">
          <label>Title</label>
          <input value={form.title} onChange={(e) => setValue("title", e.target.value)} />
          <label>BHK</label>
          <input type="number" value={form.bhk} onChange={(e) => setValue("bhk", e.target.value)} />
          <label>Area (Sqft)</label>
          <input
            type="number"
            value={form.areaSqft}
            onChange={(e) => setValue("areaSqft", e.target.value)}
          />
          <label>Expected Price</label>
          <input
            type="number"
            value={form.expectedPrice}
            onChange={(e) => setValue("expectedPrice", e.target.value)}
          />
          <label>Platform Margin</label>
          <input
            type="number"
            value={form.platformMargin}
            onChange={(e) => setValue("platformMargin", e.target.value)}
          />
        </div>
      )}

      {step === 2 && (
        <div className="form-grid">
          <label>Address</label>
          <textarea value={form.address} onChange={(e) => setValue("address", e.target.value)} />
          <label>Area</label>
          <input value={form.areaTag} onChange={(e) => setValue("areaTag", e.target.value)} />
          <label>Latitude (Map)</label>
          <input value={form.latitude} onChange={(e) => setValue("latitude", e.target.value)} />
          <label>Longitude (Map)</label>
          <input value={form.longitude} onChange={(e) => setValue("longitude", e.target.value)} />
          <p className="helper">Map view integration placeholder: store lat/lng from map picker.</p>
        </div>
      )}

      {step === 3 && (
        <div className="form-grid">
          <label>Upload Photos (one URL per line)</label>
          <textarea
            rows={8}
            value={form.photosText}
            onChange={(e) => setValue("photosText", e.target.value)}
            placeholder="https://example.com/photo1.jpg"
          />
          <p className="helper">Add nth number of photo URLs.</p>
        </div>
      )}

      <div className="row-gap">
        {step > 1 && (
          <button className="btn btn-secondary" onClick={() => setStep((s) => s - 1)}>
            Previous
          </button>
        )}
        {step < 3 ? (
          <button className="btn" onClick={() => setStep((s) => s + 1)}>
            Next
          </button>
        ) : (
          <button className="btn" onClick={submitProperty} disabled={saving}>
            {saving ? "Saving..." : "Save Property"}
          </button>
        )}
      </div>

      {error && <p className="error-text">{error}</p>}
    </div>
  );
}
