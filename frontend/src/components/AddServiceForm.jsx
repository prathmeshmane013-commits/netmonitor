import { useState } from 'react';
import { createService } from '../services/api';

const initialState = {
  name: '',
  checkType: 'HTTP',
  host: '',
  port: '',
  checkIntervalSeconds: 60,
  alertEmail: '',
};

export default function AddServiceForm({ onCreated, onClose }) {
  const [form, setForm] = useState(initialState);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      const payload = {
        name: form.name,
        checkType: form.checkType,
        host: form.host,
        checkIntervalSeconds: Number(form.checkIntervalSeconds) || 60,
        alertEmail: form.alertEmail || null,
      };
      // Only TCP_PORT checks need a port; sending it for others is harmless
      // but we keep the payload clean.
      if (form.checkType === 'TCP_PORT') {
        payload.port = Number(form.port);
      }

      await createService(payload);
      setForm(initialState);
      onCreated?.();
      onClose?.();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          'Failed to create service. Check the fields and try again.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0,0,0,0.4)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 50,
      }}
    >
      <form
        onSubmit={handleSubmit}
        style={{
          background: 'white',
          borderRadius: 10,
          padding: 24,
          width: 360,
          fontFamily: 'sans-serif',
          boxShadow: '0 4px 20px rgba(0,0,0,0.2)',
        }}
      >
        <h2 style={{ marginTop: 0 }}>Add Service to Monitor</h2>

        <label style={labelStyle}>Name</label>
        <input
          name="name"
          value={form.name}
          onChange={handleChange}
          required
          placeholder="e.g. Production API"
          style={inputStyle}
        />

        <label style={labelStyle}>Check Type</label>
        <select
          name="checkType"
          value={form.checkType}
          onChange={handleChange}
          style={inputStyle}
        >
          <option value="HTTP">HTTP (website / API health endpoint)</option>
          <option value="TCP_PORT">TCP Port (raw socket check)</option>
          <option value="PING">Ping (ICMP reachability)</option>
        </select>

        <label style={labelStyle}>
          {form.checkType === 'HTTP' ? 'URL' : 'Host / IP'}
        </label>
        <input
          name="host"
          value={form.host}
          onChange={handleChange}
          required
          placeholder={
            form.checkType === 'HTTP' ? 'https://example.com/health' : 'example.com or 8.8.8.8'
          }
          style={inputStyle}
        />

        {form.checkType === 'TCP_PORT' && (
          <>
            <label style={labelStyle}>Port</label>
            <input
              name="port"
              type="number"
              value={form.port}
              onChange={handleChange}
              required
              placeholder="443"
              style={inputStyle}
            />
          </>
        )}

        <label style={labelStyle}>Check Interval (seconds)</label>
        <input
          name="checkIntervalSeconds"
          type="number"
          value={form.checkIntervalSeconds}
          onChange={handleChange}
          min="10"
          style={inputStyle}
        />

        <label style={labelStyle}>Alert Email (optional)</label>
        <input
          name="alertEmail"
          type="email"
          value={form.alertEmail}
          onChange={handleChange}
          placeholder="you@example.com"
          style={inputStyle}
        />

        {error && <p style={{ color: 'red', fontSize: 13 }}>{error}</p>}

        <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
          <button
            type="button"
            onClick={onClose}
            style={{ flex: 1, padding: 10, background: '#e5e7eb', border: 'none', borderRadius: 6 }}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            style={{
              flex: 1,
              padding: 10,
              background: '#2563eb',
              color: 'white',
              border: 'none',
              borderRadius: 6,
              cursor: submitting ? 'not-allowed' : 'pointer',
            }}
          >
            {submitting ? 'Adding...' : 'Add Service'}
          </button>
        </div>
      </form>
    </div>
  );
}

const labelStyle = {
  display: 'block',
  fontSize: 13,
  fontWeight: 600,
  marginTop: 12,
  marginBottom: 4,
};

const inputStyle = {
  width: '100%',
  padding: 8,
  boxSizing: 'border-box',
  border: '1px solid #d1d5db',
  borderRadius: 6,
  fontSize: 14,
};
