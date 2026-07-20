import React from 'react';
import Badge from '../../components/Badge';
import { formatCurrency } from '../../utils/formatters';

const SplitContributionPanel = ({
  splitPlan,
  title = 'Team contribution split',
  subtitle,
  editable = false,
  onMemberNameChange,
}) => {
  if (!splitPlan) return null;

  const members = splitPlan.members || [];
  const paidCount = members.filter((member) => member.status === 'PAID').length;

  return (
    <section className="space-y-4 rounded-xl border border-orange-100 bg-orange-50/70 p-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h4 className="text-sm font-extrabold text-gray-900">{title}</h4>
          <p className="mt-1 text-xs font-medium text-gray-500">
            {subtitle || `${splitPlan.memberCount} members sharing ${formatCurrency(splitPlan.totalAmount)}`}
          </p>
        </div>
        <Badge status={paidCount === members.length ? 'CONFIRMED' : 'PENDING'}>
          {paidCount}/{members.length} paid
        </Badge>
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <SummaryTile label="Members" value={splitPlan.memberCount} />
        <SummaryTile label="Each contributes" value={formatCurrency(splitPlan.perMemberAmount)} tone="accent" />
        <SummaryTile label="Pending" value={formatCurrency(splitPlan.pendingAmount)} />
      </div>

      <div className="divide-y divide-orange-100 overflow-hidden rounded-lg border border-orange-100 bg-white/90">
        {members.map((member, index) => (
          <div key={member.id} className="grid grid-cols-[1fr_auto] gap-3 px-3 py-3 sm:grid-cols-[1.5fr_1fr_auto] sm:items-center">
            <div className="min-w-0">
              {editable ? (
                <input
                  value={member.name}
                  onChange={(event) => onMemberNameChange?.(index, event.target.value)}
                  aria-label={`Member ${index + 1} name`}
                  className="h-9 w-full rounded-lg border border-orange-100 bg-white px-3 text-sm font-bold text-gray-900 outline-none focus:border-accent focus:ring-2 focus:ring-orange-100"
                />
              ) : (
                <p className="truncate text-sm font-extrabold text-gray-900">{member.name}</p>
              )}
              <p className="mt-1 text-[11px] font-bold uppercase tracking-wide text-gray-400">
                Member {index + 1}
              </p>
            </div>

            <div className="text-right sm:text-left">
              <p className="text-sm font-extrabold text-gray-900">{formatCurrency(member.amount)}</p>
              <p className="mt-1 text-[11px] font-bold uppercase tracking-wide text-gray-400">Contribution</p>
            </div>

            <Badge status={member.status}>{member.status}</Badge>
          </div>
        ))}
      </div>
    </section>
  );
};

const SummaryTile = ({ label, value, tone = 'default' }) => (
  <div className="rounded-lg border border-orange-100 bg-white/80 p-3">
    <span className="block text-[10px] font-bold uppercase tracking-wide text-gray-400">{label}</span>
    <span className={`mt-1 block text-lg font-extrabold ${tone === 'accent' ? 'text-accent' : 'text-gray-900'}`}>
      {value}
    </span>
  </div>
);

export default SplitContributionPanel;
