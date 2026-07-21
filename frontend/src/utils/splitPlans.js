const STORAGE_KEY = 'turfconnect.bookingSplitPlans';

const getStorage = () => {
  if (typeof window === 'undefined') return null;
  return window.localStorage;
};

export const createSplitPlan = ({
  totalAmount = 0,
  memberCount = 6,
  memberNames = [],
  payerName = 'You',
  paidMemberIds = ['member-1'],
} = {}) => {
  const normalizedCount = Math.min(30, Math.max(1, Number(memberCount) || 1));
  const amount = Number(totalAmount) || 0;
  const baseShare = amount / normalizedCount;
  const paidIds = new Set(paidMemberIds);

  const members = Array.from({ length: normalizedCount }, (_, index) => {
    const id = `member-${index + 1}`;
    const defaultName = index === 0 ? payerName : `Player ${index + 1}`;
    const hasCustomName = Object.prototype.hasOwnProperty.call(memberNames, index);

    return {
      id,
      name: hasCustomName ? memberNames[index] : defaultName,
      amount: baseShare,
      status: paidIds.has(id) ? 'PAID' : 'PENDING',
    };
  });

  const collectedAmount = members
    .filter((member) => member.status === 'PAID')
    .reduce((sum, member) => sum + member.amount, 0);

  return {
    totalAmount: amount,
    memberCount: normalizedCount,
    perMemberAmount: baseShare,
    collectedAmount,
    pendingAmount: Math.max(amount - collectedAmount, 0),
    members,
    updatedAt: new Date().toISOString(),
  };
};

export const getBookingSplitPlans = () => {
  const storage = getStorage();
  if (!storage) return {};

  try {
    return JSON.parse(storage.getItem(STORAGE_KEY) || '{}');
  } catch {
    return {};
  }
};

export const getBookingSplitPlan = (bookingId) => {
  if (!bookingId) return null;
  return getBookingSplitPlans()[bookingId] || null;
};

export const saveBookingSplitPlan = (bookingId, splitPlan) => {
  const storage = getStorage();
  if (!storage || !bookingId || !splitPlan) return;

  const plans = getBookingSplitPlans();
  storage.setItem(STORAGE_KEY, JSON.stringify({
    ...plans,
    [bookingId]: splitPlan,
  }));
};

export const toSplitContributionRequest = (splitPlan) => {
  if (!splitPlan?.members?.length) return null;

  return {
    members: splitPlan.members.map((member) => ({
      id: member.id,
      name: member.name,
      amount: member.amount,
      status: member.status || 'PENDING',
    })),
  };
};

export const updateMemberStatus = (splitPlan, memberId, status) => {
  if (!splitPlan?.members?.length) return splitPlan;

  return createSplitPlan({
    totalAmount: splitPlan.totalAmount,
    memberCount: splitPlan.members.length,
    memberNames: splitPlan.members.map((member) => member.name),
    paidMemberIds: splitPlan.members
      .map((member) => (member.id === memberId ? { ...member, status } : member))
      .filter((member) => member.status === 'PAID')
      .map((member) => member.id),
  });
};
