import React from 'react';
import { Link } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import heroImage from '../assets/landing-turf-hero.png';

const highlights = [
  { label: 'Live slots', value: 'Real-time' },
  { label: 'Payments', value: 'Razorpay + demo' },
  { label: 'For venues', value: 'Owner tools' },
];

const featureRows = [
  {
    title: 'Book turf slots without back-and-forth calls',
    text: 'Players can discover venues, check open slots, pay, split contributions, cancel, and review in one flow.',
    tag: 'Player App',
  },
  {
    title: 'Manage grounds, bookings, and customer activity',
    text: 'Owners get a dedicated dashboard for their turfs, upcoming bookings, revenue signals, and operational status.',
    tag: 'Owner Console',
  },
  {
    title: 'Community features ready for teams and events',
    text: 'Teams, invites, tournament registration, reviews, and leaderboards give the project a complete sports platform feel.',
    tag: 'Community',
  },
];

const steps = [
  'Search by city and sport',
  'Pick a live available slot',
  'Pay using demo or Razorpay fallback',
  'Track bookings, teams, splits, and reviews',
];

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-[#f3f8fb] text-gray-950">
      <header className="absolute inset-x-0 top-0 z-20">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
          <Link to={ROUTES.LANDING} className="flex items-center gap-3 text-white">
            <span className="flex h-10 w-10 items-center justify-center rounded-lg border border-white/25 bg-white/15 text-sm font-black shadow-lg backdrop-blur">
              TC
            </span>
            <span>
              <span className="block text-xl font-black leading-none sm:text-2xl">TurfConnect</span>
              <span className="mt-1 hidden text-xs font-bold uppercase tracking-[0.18em] text-white/70 sm:block">
                Sports turf booking
              </span>
            </span>
          </Link>

          <nav className="flex items-center gap-2 sm:gap-3">
            <Link
              to={ROUTES.LOGIN}
              className="rounded-lg px-3 py-2 text-sm font-extrabold text-white transition hover:bg-white/10 sm:px-4"
            >
              Sign in
            </Link>
            <Link
              to={ROUTES.REGISTER}
              className="rounded-lg bg-white px-4 py-2 text-sm font-extrabold text-primary-dark shadow-lg shadow-black/10 transition hover:bg-green-50"
            >
              Join now
            </Link>
          </nav>
        </div>
      </header>

      <section
        className="relative min-h-[88svh] overflow-hidden bg-cover bg-center"
        style={{ backgroundImage: `url(${heroImage})` }}
      >
        <div className="absolute inset-0 bg-[#06180d]/75" />

        <div className="relative z-10 mx-auto flex min-h-[88svh] max-w-7xl flex-col justify-center px-4 pb-8 pt-24 sm:px-6 sm:pb-20 sm:pt-28 lg:px-8">
          <div className="max-w-3xl">
            <p className="mb-3 inline-flex rounded-lg border border-white/20 bg-white/10 px-3 py-2 text-[0.65rem] font-black uppercase tracking-[0.18em] text-green-100 backdrop-blur sm:mb-4 sm:text-xs">
              Turf booking for players and owners
            </p>
            <h1 className="max-w-4xl text-4xl font-black leading-[0.98] tracking-normal text-white sm:text-6xl lg:text-7xl">
              TurfConnect
            </h1>
            <p className="mt-4 max-w-2xl text-base font-semibold leading-7 text-white/80 sm:mt-5 sm:text-xl sm:leading-8">
              A complete sports venue platform for finding turfs, booking live slots, managing teams, handling payments, and running owner operations.
            </p>

            <div className="mt-6 flex flex-col gap-3 sm:mt-8 sm:flex-row">
              <Link
                to={ROUTES.REGISTER}
                className="inline-flex min-h-12 items-center justify-center rounded-lg bg-accent px-6 py-3 text-base font-black text-white shadow-xl shadow-black/20 transition hover:bg-accent-dark"
              >
                Create account
              </Link>
              <Link
                to={ROUTES.LOGIN}
                className="inline-flex min-h-12 items-center justify-center rounded-lg border border-white/25 bg-white/10 px-6 py-3 text-base font-black text-white backdrop-blur transition hover:bg-white/20"
              >
                Sign in to dashboard
              </Link>
            </div>
          </div>

          <div className="mt-6 grid max-w-3xl grid-cols-3 gap-2 sm:mt-12 sm:gap-3">
            {highlights.map((item) => (
              <div key={item.label} className="rounded-lg border border-white/20 bg-white/10 p-3 text-white shadow-xl shadow-black/10 backdrop-blur sm:p-4">
                <p className="text-[0.68rem] font-bold text-white/70 sm:text-sm">{item.label}</p>
                <p className="mt-1 text-sm font-black leading-5 sm:text-2xl">{item.value}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <main>
        <section className="border-b border-primary/10 bg-white">
          <div className="mx-auto grid max-w-7xl gap-6 px-4 py-10 sm:px-6 md:grid-cols-4 lg:px-8">
            {steps.map((step, index) => (
              <div key={step} className="flex items-start gap-4">
                <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary-light text-sm font-black text-primary-dark">
                  {index + 1}
                </span>
                <p className="text-sm font-extrabold leading-6 text-gray-800">{step}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="mx-auto grid max-w-7xl gap-8 px-4 py-14 sm:px-6 lg:grid-cols-[0.85fr_1.15fr] lg:px-8 lg:py-20">
          <div>
            <p className="text-xs font-black uppercase tracking-[0.18em] text-accent">Platform overview</p>
            <h2 className="mt-3 text-3xl font-black tracking-normal text-gray-950 sm:text-4xl">
              Built like a real turf marketplace.
            </h2>
            <p className="mt-4 max-w-xl text-base font-semibold leading-7 text-gray-600">
              The product separates customer, owner, and admin experiences while sharing one smooth booking workflow underneath.
            </p>
          </div>

          <div className="grid gap-4">
            {featureRows.map((feature) => (
              <article key={feature.title} className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <p className="text-xs font-black uppercase tracking-[0.16em] text-primary">{feature.tag}</p>
                    <h3 className="mt-2 text-xl font-black text-gray-950">{feature.title}</h3>
                    <p className="mt-2 text-sm font-semibold leading-6 text-gray-600">{feature.text}</p>
                  </div>
                  <span className="inline-flex w-fit rounded-lg bg-accent-light px-3 py-2 text-xs font-black uppercase tracking-wide text-accent-dark">
                    Ready
                  </span>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="bg-[#0c2414] px-4 py-14 text-white sm:px-6 lg:px-8">
          <div className="mx-auto grid max-w-7xl gap-8 lg:grid-cols-[1fr_1fr] lg:items-center">
            <div>
              <p className="text-xs font-black uppercase tracking-[0.18em] text-green-200">Live product preview</p>
              <h2 className="mt-3 text-3xl font-black tracking-normal sm:text-4xl">
                One project, three clear experiences.
              </h2>
              <p className="mt-4 text-base font-semibold leading-7 text-white/70">
                Players book. Owners manage. Admins supervise. TurfConnect keeps those paths separate so testing and demos feel clean.
              </p>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              {['Player dashboard', 'Owner dashboard', 'Admin panel'].map((label, index) => (
                <div key={label} className="rounded-lg border border-white/10 bg-white/10 p-4 shadow-xl shadow-black/20">
                  <p className="text-3xl font-black text-green-200">0{index + 1}</p>
                  <p className="mt-5 text-sm font-black">{label}</p>
                  <p className="mt-2 text-xs font-semibold leading-5 text-white/60">
                    Role-based routes, focused controls, and responsive screens.
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="mx-auto flex max-w-7xl flex-col gap-5 px-4 py-14 sm:px-6 md:flex-row md:items-center md:justify-between lg:px-8">
          <div>
            <h2 className="text-3xl font-black tracking-normal text-gray-950">Ready to test the platform?</h2>
            <p className="mt-2 text-sm font-semibold leading-6 text-gray-600">
              Sign in with your demo user or create a new player or owner account.
            </p>
          </div>
          <div className="flex flex-col gap-3 sm:flex-row">
            <Link
              to={ROUTES.LOGIN}
              className="inline-flex min-h-12 items-center justify-center rounded-lg bg-primary px-6 py-3 text-sm font-black text-white transition hover:bg-primary-dark"
            >
              Open login
            </Link>
            <Link
              to={ROUTES.REGISTER}
              className="inline-flex min-h-12 items-center justify-center rounded-lg border border-primary/20 bg-white px-6 py-3 text-sm font-black text-primary-dark transition hover:bg-primary-light"
            >
              Register user
            </Link>
          </div>
        </section>
      </main>
    </div>
  );
};

export default LandingPage;
