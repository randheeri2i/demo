import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";

export default function ListingsPage() {
  const [listings, setListings] = useState([]);

  const loadListings = async () => {
    const { data } = await api.get("/properties/mine");
    setListings(data.listings || []);
  };

  useEffect(() => {
    loadListings();
  }, []);

  return (
    <div className="card">
      <div className="row-between">
        <h2>My Listings</h2>
        <Link to="/listings/add" className="btn">
          Add Property
        </Link>
      </div>

      <div className="list-grid">
        {listings.map((item) => (
          <article key={item._id} className="listing-card">
            <h3>{item.title}</h3>
            <p>BHK: {item.bhk}</p>
            <p>Area: {item.areaSqft} sqft</p>
            <p>Expected Price: Rs. {(item.expectedPrice / 100000).toFixed(1)}L</p>
            <Link className="text-link" to={`/properties/${item._id}`}>
              View Detail
            </Link>
          </article>
        ))}
      </div>
    </div>
  );
}
