import React from 'react';

const teamPlaceholders = [
  ['My Teams', 'Team cards with sport, members, role, and upcoming match count.'],
  ['Team Roles', 'Captain/member role badges and team status controls.'],
];

const TeamList = () => {
  return (
    <section className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
      <p className="text-xs font-extrabold uppercase tracking-wider text-gray-500">Teams</p>
      <h2 className="mt-1 text-xl font-extrabold tracking-tight text-gray-950">Team Workspace</h2>

      <div className="mt-5 grid gap-3 md:grid-cols-2">
        {teamPlaceholders.map(([title, body]) => (
          <article key={title} className="rounded-lg border border-primary/10 bg-[#f4faff] p-4">
            <h3 className="text-sm font-extrabold text-gray-950">{title}</h3>
            <p className="mt-2 text-sm font-medium text-gray-500">{body}</p>
          </article>
        ))}
      </div>
    </section>
  );
};

export default TeamList;
