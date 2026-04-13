import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";

export default function PropertyDetailPage() {
  const { propertyId } = useParams();
  const [property, setProperty] = useState(null);
  const [index, setIndex] = useState(0);
  const [visitForm, setVisitForm] = useState({ buyerName: "", buyerPhone: "", date: "", time: "", notes: "" });
  const [message, setMessage] = useState("");

  const loadProperty = async () => {
    const { data } = await api.get(`/properties/${propertyId}`);
    setProperty(data.property);
  };

  useEffect(() => {
    loadProperty();
  }, [propertyId]);

  const currentImage = useMemo(() => {
    if (!property?.photos?.length) return "";
    return property.photos[index % property.photos.length];
  }, [property, index]);

  const scheduleVisit = async (e) => {
    e.preventDefault();
    setMessage("");

    const scheduledAt = new Date(`${visitForm.date}T${visitForm.time}`).toISOString();
    await api.post("/visits", {
      propertyId,
      buyerName: visitForm.buyerName,
      buyerPhone: visitForm.buyerPhone,
      scheduledAt,
      notes: visitForm.notes,
    });

    setMessage("Visit confirmed");
    setVisitForm({ buyerName: "", buyerPhone: "", date: "", time: "", notes: "" });
  };

  if (!property) {
    return <div className="loading-state">Loading property...</div>;
  }

  return (
    <div className="page-grid">
      <section className="card">
        <h2>{property.title}</h2>

        {property.photos?.length ? (
          <div className="carousel">
            <img src={currentImage} alt={property.title} />
            <div className="row-gap">
              <button className="btn btn-secondary" onClick={() => setIndex((i) => (i - 1 + property.photos.length) % property.photos.length)}>
                Prev
              </button>
              <button className="btn btn-secondary" onClick={() => setIndex((i) => (i + 1) % property.photos.length)}>
                Next
              </button>
            </div>
          </div>
        ) : (
          <p className="muted">No photos uploaded</p>
        )}

        <div className="detail-grid">
          <p>
            Price: <strong>Rs. {(property.expectedPrice / 100000).toFixed(1)}L</strong>
          </p>
          <p>
            BHK: <strong>{property.bhk}</strong>
          </p>
          <p>
            Plateform Margin: <strong>Rs. {(property.platformMargin / 100000).toFixed(1)}L</strong>
          </p>
          <p>
            Area: <strong>{property.areaTag || "Noida"}</strong>
          </p>
        </div>
      </section>

      <section className="card">
        <h2>Schedule Visit</h2>
        <form className="form-grid" onSubmit={scheduleVisit}>
          <label>Buyer Name</label>
          <input
            value={visitForm.buyerName}
            onChange={(e) => setVisitForm((p) => ({ ...p, buyerName: e.target.value }))}
            required
          />

          <label>Buyer Mobile</label>
          <input
            value={visitForm.buyerPhone}
            onChange={(e) => setVisitForm((p) => ({ ...p, buyerPhone: e.target.value }))}
            required
          />

          <label>Choose Date</label>
          <input
            type="date"
            value={visitForm.date}
            onChange={(e) => setVisitForm((p) => ({ ...p, date: e.target.value }))}
            required
          />

          <label>Choose Time</label>
          <input
            type="time"
            value={visitForm.time}
            onChange={(e) => setVisitForm((p) => ({ ...p, time: e.target.value }))}
            required
          />

          <label>Notes</label>
          <textarea
            value={visitForm.notes}
            onChange={(e) => setVisitForm((p) => ({ ...p, notes: e.target.value }))}
          />

          <button className="btn" type="submit">
            Confirm
          </button>
        </form>

        {message && <p className="success-text">{message}</p>}
      </section>
    </div>
  );
}
