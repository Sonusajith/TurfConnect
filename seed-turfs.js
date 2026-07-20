const http = require('http');
const https = require('https');

const API_GATEWAY = process.env.API_GATEWAY || process.env.API_BASE_URL || 'http://localhost:8090/api/v1';
const OWNER_EMAIL = process.env.SEED_OWNER_EMAIL || 'seed.owner@turfconnect.test';
const OWNER_PASSWORD = process.env.SEED_OWNER_PASSWORD || 'Password123!';

const allDays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const demoTurfs = [
  {
    name: 'GreenField Arena',
    description: 'Demo 5-a-side football turf for quick local bookings and evening practice games.',
    sportTypes: ['FOOTBALL', 'CRICKET'],
    address: 'MG Road Sports Complex',
    city: 'Bengaluru',
    state: 'Karnataka',
    postalCode: '560001',
    longitude: 77.5946,
    latitude: 12.9716,
    hourlyRate: 1200,
    amenities: ['Floodlights', 'Parking', 'Changing Rooms', 'Drinking Water'],
    coverImage: 'https://images.unsplash.com/photo-1556056504-5c7696c4c28d?auto=format&fit=crop&w=1200&q=80',
    openTime: '06:00',
    closeTime: '22:00',
    capacity: 12,
    surfaceType: 'Artificial Turf',
    indoorOrOutdoor: 'OUTDOOR',
    foodAvailable: true,
  },
  {
    name: 'Koramangala Kickoff',
    description: 'Compact football-first venue with late-night lights and fast synthetic grass.',
    sportTypes: ['FOOTBALL'],
    address: '7th Block Play Street',
    city: 'Bengaluru',
    state: 'Karnataka',
    postalCode: '560095',
    longitude: 77.6245,
    latitude: 12.9352,
    hourlyRate: 950,
    amenities: ['Floodlights', 'Parking', 'Washrooms', 'Bibs'],
    coverImage: 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=1200&q=80',
    openTime: '05:00',
    closeTime: '23:00',
    capacity: 10,
    surfaceType: 'Synthetic Grass',
    indoorOrOutdoor: 'OUTDOOR',
    equipmentRentalAvailable: true,
  },
  {
    name: 'Indiranagar Box Cricket',
    description: 'Netted cricket box with score lights, short boundaries, and team benches.',
    sportTypes: ['CRICKET'],
    address: 'CMH Road Indoor Grounds',
    city: 'Bengaluru',
    state: 'Karnataka',
    postalCode: '560038',
    longitude: 77.6408,
    latitude: 12.9784,
    hourlyRate: 1400,
    amenities: ['Net Cage', 'Floodlights', 'Scoreboard', 'Drinking Water'],
    coverImage: 'https://images.unsplash.com/photo-1531415074968-036ba1b575da?auto=format&fit=crop&w=1200&q=80',
    openTime: '06:00',
    closeTime: '22:00',
    capacity: 14,
    surfaceType: 'Matting',
    indoorOrOutdoor: 'INDOOR',
    equipmentRentalAvailable: true,
  },
  {
    name: 'Hyderabad Smash Hub',
    description: 'Indoor badminton and multi-sport court near HITEC City with coaching support.',
    sportTypes: ['BADMINTON', 'TENNIS'],
    address: 'HITEC City Sports Lane',
    city: 'Hyderabad',
    state: 'Telangana',
    postalCode: '500081',
    longitude: 78.3762,
    latitude: 17.4435,
    hourlyRate: 800,
    amenities: ['Indoor Court', 'Coaching', 'Changing Rooms', 'Parking'],
    coverImage: 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=1200&q=80',
    openTime: '06:00',
    closeTime: '21:00',
    capacity: 8,
    surfaceType: 'Wooden Court',
    indoorOrOutdoor: 'INDOOR',
    coachingAvailable: true,
  },
  {
    name: 'Chennai Turf Bay',
    description: 'Sea-breeze outdoor football and cricket venue with weekend tournament slots.',
    sportTypes: ['FOOTBALL', 'CRICKET'],
    address: 'OMR Sports Yard',
    city: 'Chennai',
    state: 'Tamil Nadu',
    postalCode: '600097',
    longitude: 80.2297,
    latitude: 12.9051,
    hourlyRate: 1100,
    amenities: ['Floodlights', 'Food Counter', 'Parking', 'Washrooms'],
    coverImage: 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1200&q=80',
    openTime: '05:00',
    closeTime: '22:00',
    capacity: 16,
    surfaceType: 'Artificial Turf',
    indoorOrOutdoor: 'OUTDOOR',
    foodAvailable: true,
  },
  {
    name: 'Mumbai Cage Football',
    description: 'Urban cage football court for high-tempo 5v5 games close to Bandra.',
    sportTypes: ['FOOTBALL'],
    address: 'Bandra West Recreation Ground',
    city: 'Mumbai',
    state: 'Maharashtra',
    postalCode: '400050',
    longitude: 72.8295,
    latitude: 19.0596,
    hourlyRate: 1800,
    amenities: ['Cage Nets', 'Floodlights', 'Parking', 'Water Station'],
    coverImage: 'https://images.unsplash.com/photo-1518604666860-9ed391f76460?auto=format&fit=crop&w=1200&q=80',
    openTime: '06:00',
    closeTime: '23:00',
    capacity: 10,
    surfaceType: 'Artificial Turf',
    indoorOrOutdoor: 'OUTDOOR',
  },
  {
    name: 'Pune Rally Courts',
    description: 'Clean tennis-first venue with predictable hourly slots and training add-ons.',
    sportTypes: ['TENNIS', 'BADMINTON'],
    address: 'Baner Sports Campus',
    city: 'Pune',
    state: 'Maharashtra',
    postalCode: '411045',
    longitude: 73.7744,
    latitude: 18.559,
    hourlyRate: 1000,
    amenities: ['Coaching', 'Equipment Rental', 'Washrooms', 'Parking'],
    coverImage: 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?auto=format&fit=crop&w=1200&q=80',
    openTime: '06:00',
    closeTime: '20:00',
    capacity: 4,
    surfaceType: 'Acrylic Court',
    indoorOrOutdoor: 'OUTDOOR',
    coachingAvailable: true,
    equipmentRentalAvailable: true,
  },
];

async function request(method, path, data = null, token = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(API_GATEWAY + path);
    const transport = url.protocol === 'https:' ? https : http;
    const options = {
      hostname: url.hostname,
      port: url.port || (url.protocol === 'https:' ? 443 : 80),
      path: url.pathname + url.search,
      method,
      headers: {
        'Content-Type': 'application/json',
      },
    };

    if (token) {
      options.headers.Authorization = `Bearer ${token}`;
    }

    const req = transport.request(options, (res) => {
      let body = '';
      res.on('data', chunk => {
        body += chunk;
      });
      res.on('end', () => {
        let parsed = null;
        try {
          parsed = body ? JSON.parse(body) : null;
        } catch (error) {
          reject(error);
          return;
        }

        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(parsed);
        } else {
          reject(new Error(`Status: ${res.statusCode}, Body: ${body}`));
        }
      });
    });

    req.on('error', reject);
    if (data) {
      req.write(JSON.stringify(data));
    }
    req.end();
  });
}

function toPayload(turf) {
  return {
    country: 'India',
    timezone: 'Asia/Kolkata',
    currency: 'INR',
    slotDurationMinutes: 60,
    availableDays: allDays,
    contactNumber: '9999999999',
    email: `${turf.name.toLowerCase().replace(/[^a-z0-9]+/g, '.').replace(/\.$/, '')}@turfconnect.test`,
    floodlightsAvailable: turf.amenities.includes('Floodlights'),
    parkingAvailable: turf.amenities.includes('Parking'),
    changingRoomsAvailable: turf.amenities.includes('Changing Rooms'),
    washroomsAvailable: turf.amenities.includes('Washrooms'),
    drinkingWaterAvailable: turf.amenities.includes('Drinking Water') || turf.amenities.includes('Water Station'),
    equipmentRentalAvailable: Boolean(turf.equipmentRentalAvailable),
    foodAvailable: Boolean(turf.foodAvailable),
    coachingAvailable: Boolean(turf.coachingAvailable),
    ...turf,
  };
}

async function ensureOwner() {
  try {
    await request('POST', '/auth/register', {
      email: OWNER_EMAIL,
      password: OWNER_PASSWORD,
      name: 'Seed Turf Owner',
      role: 'TURF_OWNER',
    });
    console.log(`Registered owner ${OWNER_EMAIL}`);
  } catch (error) {
    console.log(`Owner registration skipped: ${error.message}`);
  }

  const loginRes = await request('POST', '/auth/login', {
    email: OWNER_EMAIL,
    password: OWNER_PASSWORD,
  });
  return loginRes.data.accessToken;
}

async function seed() {
  console.log(`Using API gateway: ${API_GATEWAY}`);
  const token = await ensureOwner();
  const myTurfsRes = await request('GET', '/turfs/my-turfs?size=100', null, token);
  const existingTurfs = myTurfsRes.data.content || [];

  for (const turf of demoTurfs) {
    const payload = toPayload(turf);
    const existing = existingTurfs.find(item =>
      item.name.toLowerCase() === payload.name.toLowerCase() &&
      item.city.toLowerCase() === payload.city.toLowerCase()
    );

    if (existing) {
      await request('PUT', `/turfs/${existing.id}`, { ...payload, status: 'ACTIVE' }, token);
      console.log(`Updated ${payload.name} (${payload.city})`);
      continue;
    }

    const created = await request('POST', '/turfs', payload, token);
    await request('PUT', `/turfs/${created.data.id}`, { status: 'ACTIVE', coverImage: payload.coverImage }, token);
    console.log(`Created ${payload.name} (${payload.city})`);
  }

  console.log('Demo turf seed complete.');
}

seed().catch((error) => {
  console.error('Error seeding turfs:', error.message || error);
  process.exitCode = 1;
});
