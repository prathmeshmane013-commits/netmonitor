import { useEffect, useState } from "react";
import { getServices, getUptime, deleteService } from "../services/api";
import AddServiceForm from "./AddServiceForm";

const STATUS_COLORS = {
  UP: "#22c55e",
  DOWN: "#ef4444",
  UNKNOWN: "#9ca3af",
};

export default function Dashboard() {
  const [services, setServices] = useState([]);
  const [uptimes, setUptimes] = useState({});
  const [loading, setLoading] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);

  const loadData = async () => {
    try {
      const { data } = await getServices();
      setServices(data);

      const uptimeEntries = await Promise.all(
        data.map(async (s) => {
          const { data: up } = await getUptime(s.id, 24);
          return [s.id, up];
        }),
      );
      setUptimes(Object.fromEntries(uptimeEntries));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 15000); // poll every 15s
    return () => clearInterval(interval);
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Remove this service from monitoring?")) return;
    await deleteService(id);
    loadData();
  };

  if (loading) return <p>Loading dashboard...</p>;

  return (
    <div style={{ padding: "24px", fontFamily: "sans-serif" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <h1>Network Monitor Dashboard</h1>
        <button
          onClick={() => setShowAddForm(true)}
          style={{
            padding: "10px 16px",
            background: "#2563eb",
            color: "white",
            border: "none",
            borderRadius: 6,
            cursor: "pointer",
            fontWeight: 600,
          }}
        >
          + Add Service
        </button>
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))",
          gap: "16px",
          marginTop: "20px",
        }}
      >
        {services.length === 0 && (
          <p style={{ color: "#9ca3af" }}>
            No services yet. Click "+ Add Service" to start monitoring
            something.
          </p>
        )}

        {services.map((service) => (
          <div
            key={service.id}
            style={{
              border: "1px solid #e5e7eb",
              borderRadius: "10px",
              padding: "16px",
              boxShadow: "0 1px 3px rgba(0,0,0,0.08)",
              position: "relative",
            }}
          >
            <button
              onClick={() => handleDelete(service.id)}
              title="Remove"
              style={{
                position: "absolute",
                top: 10,
                right: 10,
                border: "none",
                background: "transparent",
                color: "#9ca3af",
                cursor: "pointer",
                fontSize: 14,
              }}
            >
              ✕
            </button>

            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <strong>{service.name}</strong>
              <span
                style={{
                  width: 12,
                  height: 12,
                  borderRadius: "50%",
                  backgroundColor:
                    STATUS_COLORS[service.currentStatus] || "#9ca3af",
                  display: "inline-block",
                  marginRight: 18,
                }}
                title={service.currentStatus}
              />
            </div>
            <p style={{ color: "#6b7280", fontSize: "13px", margin: "8px 0" }}>
              {service.checkType} &middot; {service.host}
              {service.port ? `:${service.port}` : ""}
            </p>
            <p style={{ fontSize: "13px" }}>
              Uptime (24h): {uptimes[service.id]?.uptimePercentage ?? "—"}%
            </p>
            <p style={{ fontSize: "12px", color: "#9ca3af" }}>
              Last checked:{" "}
              {service.lastCheckedAt
                ? new Date(service.lastCheckedAt).toLocaleTimeString()
                : "never"}
            </p>
          </div>
        ))}
      </div>

      {showAddForm && (
        <AddServiceForm
          onCreated={loadData}
          onClose={() => setShowAddForm(false)}
        />
      )}
    </div>
  );
}
