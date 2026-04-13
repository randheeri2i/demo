import { useEffect, useState } from "react";
import { api } from "../api/client";

export default function DealsPage() {
  const [deals, setDeals] = useState([]);
  const [listings, setListings] = useState([]);
  const [form, setForm] = useState({ propertyId: "", buyerName: "", visitStatus: "Scheduled" });

  const loadData = async () => {
    const [dealsRes, listingsRes] = await Promise.all([api.get("/deals/my-deals"), api.get("/properties/mine")]);
    setDeals(dealsRes.data.deals || []);
    setListings(listingsRes.data.listings || []);
  };

  useEffect(() => {
    loadData();
  }, []);

  const createDeal = async (e) => {
    e.preventDefault();
    await api.post("/deals", form);
    setForm({ propertyId: "", buyerName: "", visitStatus: "Scheduled" });
    loadData();
  };

  const updateStatus = async (dealId, visitStatus) => {
    await api.patch(`/deals/${dealId}/status`, { visitStatus });
    loadData();
  };

  return (
    <div className="page-grid">
      <section className="card">
        <h2>My Deals</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Property</th>
                <th>Buyer</th>
                <th>Status of visit</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {deals.map((deal) => (
                <tr key={deal._id}>
                  <td>{deal.propertyTitle}</td>
                  <td>{deal.buyerName}</td>
                  <td>{deal.visitStatus}</td>
                  <td>
                    <select
                      value={deal.visitStatus}
                      onChange={(e) => updateStatus(deal._id, e.target.value)}
                    >
                      <option>Scheduled</option>
                      <option>Visited</option>
                      <option>Negotiating</option>
                      <option>Closed</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="card">
        <h2>Add Deal</h2>
        <form className="form-grid" onSubmit={createDeal}>
          <label>Property</label>
          <select
            value={form.propertyId}
            onChange={(e) => setForm((p) => ({ ...p, propertyId: e.target.value }))}
            required
          >
            <option value="">Select</option>
            {listings.map((property) => (
              <option key={property._id} value={property._id}>
                {property.title}
              </option>
            ))}
          </select>

          <label>Buyer Name</label>
          <input
            value={form.buyerName}
            onChange={(e) => setForm((p) => ({ ...p, buyerName: e.target.value }))}
            required
          />

          <label>Status of visit</label>
          <select
            value={form.visitStatus}
            onChange={(e) => setForm((p) => ({ ...p, visitStatus: e.target.value }))}
          >
            <option>Scheduled</option>
            <option>Visited</option>
            <option>Negotiating</option>
            <option>Closed</option>
          </select>

          <button className="btn" type="submit">
            Save Deal
          </button>
        </form>
      </section>
    </div>
  );
}
