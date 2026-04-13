import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";

export default function HomePage() {
  const [summary, setSummary] = useState({ activeDeals: 0, visitsToday: 0 });
  const [hotListings, setHotListings] = useState([]);
  const [search, setSearch] = useState("");

  const loadHomeData = async () => {
    const [summaryRes, listingsRes] = await Promise.all([
      api.get("/dashboard/summary"),
      api.get("/properties/hot", { params: { search, area: "Noida" } }),
    ]);

    setSummary(summaryRes.data);
    setHotListings(listingsRes.data.listings || []);
  };

  useEffect(() => {
    loadHomeData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search]);

  return (
    <div className="page-grid">
      <section className="card">
        <h2>My Activity</h2>
        <div className="stats-grid">
          <div className="stat-card">
            <span>Active Deals</span>
            <strong>{summary.activeDeals}</strong>
          </div>
          <div className="stat-card">
            <span>Visits Today</span>
            <strong>{summary.visitsToday}</strong>
          </div>
        </div>
      </section>

      <section className="card">
        <div className="row-between">
          <h2>Hot Listings</h2>
          <input
            className="input-inline"
            placeholder="Search property"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="list-grid">
          {hotListings.map((item) => (
            <article key={item._id} className="listing-card">
              <p className="muted">Area: {item.areaTag || "Noida"}</p>
              <h3>{item.title}</h3>
              <p>
                Pricing: <strong>Rs. {(item.expectedPrice / 100000).toFixed(1)}L</strong>
              </p>
              <p>BHK: {item.bhk}</p>
              <Link className="text-link" to={`/properties/${item._id}`}>
                More Details
              </Link>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
