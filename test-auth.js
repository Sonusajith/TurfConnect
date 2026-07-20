const registerBody = { name: "Rohit Test", email: "test@example.com", password: "Password123!", role: "PLAYER" };

async function testAuth() {
  try {
    console.log("Testing Registration...");
    const res = await fetch("http://localhost:8080/api/v1/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(registerBody)
    });
    console.log("Register Status:", res.status);
    const text = await res.text();
    console.log("Register Response:", text);

    console.log("\nTesting Login...");
    const loginRes = await fetch("http://localhost:8080/api/v1/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: "test@example.com", password: "Password123!" })
    });
    console.log("Login Status:", loginRes.status);
    const loginText = await loginRes.text();
    console.log("Login Response:", loginText);
  } catch (err) {
    console.error("Fetch Error:", err);
  }
}
testAuth();
