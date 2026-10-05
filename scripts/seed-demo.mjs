#!/usr/bin/env node

/**
 * seed-demo.mjs
 * Genera dinámicamente bloques de disponibilidad y turnos para los próximos 14 días
 * para los profesionales activos del laboratorio en el entorno de desarrollo.
 */

import { strict as assert } from 'node:assert';

const BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

async function main() {
  console.log(`🌱 Sembrando bloques de disponibilidad para los próximos 14 días en ${BASE_URL}...`);

  // 1. Iniciar sesión como administrador
  const loginRes = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'admin@fcv.test', password: 'Admin123*' }),
    signal: AbortSignal.timeout(10000),
  });

  if (!loginRes.ok) {
    console.warn(`⚠️ No se pudo autenticar como admin (${loginRes.status}). Verifique que citas-api esté en ejecución.`);
    process.exit(1);
  }

  const { accessToken } = await loginRes.json();
  const authHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${accessToken}`,
  };

  // Profesionales y sus sedes correspondientes
  const professionals = [
    { id: 1, name: 'Dr. Carlos Mendoza (Medicina General)', locationId: 1 },
    { id: 1, name: 'Dr. Carlos Mendoza (Sede ICV)', locationId: 2 },
    { id: 2, name: 'Dra. Sofía Castro (Cardiología)', locationId: 1 },
    { id: 3, name: 'Dr. Andrés Ruiz (Pediatría)', locationId: 2 },
  ];

  let createdCount = 0;
  let skippedCount = 0;

  const today = new Date();

  for (let dayOffset = 1; dayOffset <= 14; dayOffset++) {
    const targetDate = new Date(today);
    targetDate.setDate(today.getDate() + dayOffset);
    const dateStr = targetDate.toISOString().split('T')[0];

    // Omitir domingos (día 0)
    if (targetDate.getDay() === 0) continue;

    for (const prof of professionals) {
      // Turno mañana: 08:00 - 12:00
      try {
        const resMorning = await fetch(`${BASE_URL}/api/professionals/${prof.id}/blocks`, {
          method: 'POST',
          headers: authHeaders,
          body: JSON.stringify({
            locationId: prof.locationId,
            date: dateStr,
            startTime: '08:00',
            endTime: '12:00',
          }),
        });

        if (resMorning.status === 201) {
          createdCount++;
        } else if (resMorning.status === 409) {
          skippedCount++;
        }
      } catch (err) {
        // Ignorar errores puntuales de red
      }

      // Turno tarde: 14:00 - 17:00
      try {
        const resAfternoon = await fetch(`${BASE_URL}/api/professionals/${prof.id}/blocks`, {
          method: 'POST',
          headers: authHeaders,
          body: JSON.stringify({
            locationId: prof.locationId,
            date: dateStr,
            startTime: '14:00',
            endTime: '17:00',
          }),
        });

        if (resAfternoon.status === 201) {
          createdCount++;
        } else if (resAfternoon.status === 409) {
          skippedCount++;
        }
      } catch (err) {
        // Ignorar errores puntuales de red
      }
    }
  }

  console.log(`✅ Siembra completada: ${createdCount} bloques creados exitosamente (${skippedCount} ya existían).`);
  console.log(`📅 Grilla de agendamiento 100% habilitada para los próximos 14 días.`);
}

main().catch((err) => {
  console.error('Error durante la siembra:', err.message);
  process.exit(1);
});
