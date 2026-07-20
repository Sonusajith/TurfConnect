const http = require('http');

const API_GATEWAY = 'http://localhost:8080/api/v1';

async function request(method, path, data = null, token = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(API_GATEWAY + path);
    const options = {
      hostname: url.hostname,
      port: url.port,
      path: url.pathname + url.search,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      }
    };
    if (token) {
      options.headers['Authorization'] = 'Bearer ' + token;
    }

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(body ? JSON.parse(body) : null);
          } else {
            reject(`Status: ${res.statusCode}, Body: ${body}`);
          }
        } catch (e) {
          reject(e);
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

async function seed() {
  try {
    console.log("Registering owner...");
    try {
      await request('POST', '/auth/register', {
        email: 'owner2@example.com',
        password: 'Password123!',
        firstName: 'Turf',
        lastName: 'Owner',
        phoneNumber: '9876543210',
        role: 'TURF_OWNER'
      });
      console.log("Owner registered.");
    } catch (e) {
      console.log("Registration error:", e);
    }

    console.log("Logging in...");
    const loginRes = await request('POST', '/auth/login', {
      email: 'owner2@example.com',
      password: 'Password123!'
    });
    const token = loginRes.data.accessToken;

    console.log("Creating Turf in Bengaluru...");
    const turfRes = await request('POST', '/turfs', {
      name: 'Greenfield Arena',
      description: 'Premium artificial grass football turf in the heart of Bengaluru.',
      sportTypes: ['FOOTBALL', 'CRICKET'],
      address: '123 Sports Avenue',
      city: 'Bengaluru',
      state: 'Karnataka',
      country: 'India',
      postalCode: '560001',
      longitude: 77.5946,
      latitude: 12.9716,
      timezone: 'Asia/Kolkata',
      hourlyRate: 1500,
      currency: 'INR',
      amenities: ['Parking', 'Washroom', 'Floodlights', 'Drinking Water'],
      openTime: '06:00',
      closeTime: '23:00',
      slotDurationMinutes: 60,
      availableDays: ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'],
      contactNumber: '9999999999',
      email: 'contact@greenfieldarena.com',
      capacity: 14,
      surfaceType: 'ARTIFICIAL_GRASS',
      indoorOrOutdoor: 'OUTDOOR',
      floodlightsAvailable: true,
      parkingAvailable: true,
      changingRoomsAvailable: true,
      washroomsAvailable: true,
      drinkingWaterAvailable: true,
      equipmentRentalAvailable: true,
      foodAvailable: false,
      coachingAvailable: false
    }, token);

    console.log("Turf created successfully! ID:", turfRes.data.id);
  } catch (error) {
    console.error("Error seeding turf:", error);
  }
}

seed();
