import React, { useState, useEffect, useCallback } from 'react';
import { useToast } from '../hooks/useToast';
import { useAuth } from '../hooks/useAuth';
import { API_BASE_URL, API_ENDPOINTS } from '../constants/api';
import { Card } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Skeleton from '../components/ui/Skeleton';

const AdminUsersPage = () => {
  const { token, user } = useAuth();
  const { addToast } = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const size = 10;
  
  // Modals
  const [actionUser, setActionUser] = useState(null);
  const [actionType, setActionType] = useState(null); // 'ROLE', 'STATUS', 'DELETE'
  const [actionValue, setActionValue] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  // Create User State
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createForm, setCreateForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'PLAYER'
  });

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ADMIN.USERS}?page=${page}&size=${size}&search=${encodeURIComponent(searchTerm)}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      const text = await res.text();
      const data = text ? JSON.parse(text) : {};
      
      if (res.ok && data.success) {
        setUsers(data.data.content || []);
        setTotalPages(data.data.totalPages || 1);
      } else {
        addToast(data.message || 'Failed to fetch users', 'error');
      }
    } catch {
      addToast('Network error while fetching users', 'error');
    } finally {
      setLoading(false);
    }
  }, [addToast, page, searchTerm, token]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    if (page === 0) {
      fetchUsers();
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ADMIN.USERS}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(createForm)
      });
      const text = await res.text();
      const data = text ? JSON.parse(text) : {};
      
      if (res.ok && data.success) {
        addToast(data.message || 'User created successfully', 'success');
        setShowCreateModal(false);
        setCreateForm({ name: '', email: '', password: '', role: 'PLAYER' });
        setPage(0);
        fetchUsers(); // Refresh list
      } else {
        addToast(data.message || 'Failed to create user', 'error');
      }
    } catch {
      addToast('Network error while creating user', 'error');
    } finally {
      setCreateLoading(false);
    }
  };

  const handleAction = async () => {
    if (!actionUser || !actionType) return;
    
    setActionLoading(true);
    let url = `${API_BASE_URL}${API_ENDPOINTS.ADMIN.USERS}/${actionUser.id}`;
    let method = 'PUT';
    let body = {};
    
    if (actionType === 'ROLE') {
      url += '/role';
      body = { role: actionValue };
    } else if (actionType === 'STATUS') {
      url += '/status';
      body = { status: actionValue };
    } else if (actionType === 'DELETE') {
      method = 'DELETE';
    }

    try {
      const res = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: method !== 'DELETE' ? JSON.stringify(body) : undefined
      });
      const text = await res.text();
      const data = text ? JSON.parse(text) : {};
      
      if (res.ok && data.success) {
        addToast(data.message || 'Action completed successfully', 'success');
        setActionUser(null);
        fetchUsers(); // Refresh
      } else {
        addToast(data.message || 'Action failed', 'error');
      }
    } catch {
      addToast('Network error during action', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const openActionModal = (u, type) => {
    setActionUser(u);
    setActionType(type);
    if (type === 'ROLE') setActionValue(u.role);
    if (type === 'STATUS') setActionValue(u.accountStatus === 'LOCKED' ? 'ACTIVE' : 'LOCKED');
  };

  return (
    <div className="space-y-6">
      <div className="border-b border-primary/10 pb-5">
        <h1 className="text-2xl font-extrabold text-gray-950 tracking-tight">User Management</h1>
        <p className="mt-2 text-sm font-semibold text-gray-500">Manage players, owners, and administrators.</p>
      </div>

      <Card>
        <div className="p-4 sm:p-6 space-y-4">
          <div className="flex flex-col sm:flex-row justify-between gap-4">
            <form onSubmit={handleSearch} className="flex gap-3 flex-1">
              <input 
                type="text" 
                placeholder="Search by name or email..." 
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="flex-1 max-w-md rounded-lg border border-gray-300 px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
              />
              <Button type="submit">Search</Button>
            </form>
            <Button onClick={() => setShowCreateModal(true)} variant="primary" className="whitespace-nowrap">
              + Add New User
            </Button>
          </div>

          {loading ? (
            <div className="space-y-3">
               <Skeleton className="h-12 w-full" />
               <Skeleton className="h-12 w-full" />
               <Skeleton className="h-12 w-full" />
            </div>
          ) : users.length === 0 ? (
            <div className="py-12 text-center text-gray-500 font-semibold">No users found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-gray-600">
                <thead className="border-b border-gray-200 bg-gray-50 text-xs font-bold uppercase tracking-wider text-gray-500">
                  <tr>
                    <th className="px-4 py-3">Name / Email</th>
                    <th className="px-4 py-3">Role</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 bg-white">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3">
                        <p className="font-bold text-gray-900">{u.name}</p>
                        <p className="text-xs text-gray-500">{u.email}</p>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={u.role === 'SUPER_ADMIN' ? 'error' : (u.role === 'TURF_OWNER' ? 'primary' : 'success')}>
                          {u.role?.replace('_', ' ')}
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={u.accountStatus === 'LOCKED' ? 'error' : 'success'}>
                          {u.accountStatus}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-right space-x-2">
                        {u.id !== user.userId && (
                          <>
                            <button onClick={() => openActionModal(u, 'ROLE')} className="text-xs font-bold text-primary hover:underline">Change Role</button>
                            <span className="text-gray-300">|</span>
                            <button onClick={() => openActionModal(u, 'STATUS')} className={`text-xs font-bold hover:underline ${u.accountStatus === 'LOCKED' ? 'text-green-600' : 'text-orange-500'}`}>
                              {u.accountStatus === 'LOCKED' ? 'Unlock' : 'Lock'}
                            </button>
                            <span className="text-gray-300">|</span>
                            <button onClick={() => openActionModal(u, 'DELETE')} className="text-xs font-bold text-red-600 hover:underline">Delete</button>
                          </>
                        )}
                        {u.id === user.userId && (
                          <span className="text-xs italic text-gray-400">Current User</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between pt-4 border-t border-gray-100">
              <Button 
                variant="outline" 
                size="sm" 
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <span className="text-sm font-semibold text-gray-600">Page {page + 1} of {totalPages}</span>
              <Button 
                variant="outline" 
                size="sm" 
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
              >
                Next
              </Button>
            </div>
          )}
        </div>
      </Card>

      {/* Action Modal */}
      {actionUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm p-4">
          <Card className="w-full max-w-md animate-in fade-in zoom-in duration-200">
            <div className="p-6">
              <h3 className="text-lg font-bold text-gray-900 mb-4">
                {actionType === 'ROLE' && 'Change User Role'}
                {actionType === 'STATUS' && (actionUser.accountStatus === 'LOCKED' ? 'Unlock Account' : 'Lock Account')}
                {actionType === 'DELETE' && 'Delete User Account'}
              </h3>
              
              <div className="mb-6 space-y-4">
                <p className="text-sm text-gray-600">
                  Target User: <span className="font-bold text-gray-900">{actionUser.name} ({actionUser.email})</span>
                </p>

                {actionType === 'ROLE' && (
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">New Role</label>
                    <select 
                      value={actionValue} 
                      onChange={(e) => setActionValue(e.target.value)}
                      className="w-full rounded-lg border border-gray-300 p-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                    >
                      <option value="PLAYER">PLAYER</option>
                      <option value="TURF_OWNER">TURF_OWNER</option>
                      <option value="ORG_ADMIN">ORG_ADMIN</option>
                      <option value="SUPER_ADMIN">SUPER_ADMIN</option>
                    </select>
                  </div>
                )}

                {actionType === 'STATUS' && (
                  <p className="text-sm text-red-600 font-semibold bg-red-50 p-3 rounded-lg border border-red-100">
                    Are you sure you want to {actionValue === 'LOCKED' ? 'lock' : 'unlock'} this account? 
                    {actionValue === 'LOCKED' ? ' They will be logged out and unable to log in.' : ''}
                  </p>
                )}

                {actionType === 'DELETE' && (
                  <p className="text-sm text-red-600 font-semibold bg-red-50 p-3 rounded-lg border border-red-100">
                    WARNING: This action is permanent and cannot be undone. All user data, including refresh tokens, will be deleted.
                  </p>
                )}
              </div>

              <div className="flex justify-end gap-3">
                <Button variant="outline" onClick={() => setActionUser(null)} disabled={actionLoading}>Cancel</Button>
                <Button 
                  onClick={handleAction} 
                  loading={actionLoading}
                  variant={actionType === 'DELETE' ? 'danger' : 'primary'}
                >
                  Confirm Action
                </Button>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Create User Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm p-4">
          <Card className="w-full max-w-md animate-in fade-in zoom-in duration-200">
            <div className="p-6">
              <h3 className="text-xl font-bold text-gray-900 mb-4">Add New User</h3>
              <form onSubmit={handleCreateUser} className="space-y-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Full Name</label>
                  <input 
                    type="text" 
                    required 
                    value={createForm.name}
                    onChange={(e) => setCreateForm({...createForm, name: e.target.value})}
                    className="w-full rounded-lg border border-gray-300 p-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Email Address</label>
                  <input 
                    type="email" 
                    required 
                    value={createForm.email}
                    onChange={(e) => setCreateForm({...createForm, email: e.target.value})}
                    className="w-full rounded-lg border border-gray-300 p-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Password</label>
                  <input 
                    type="password" 
                    required 
                    minLength={8}
                    value={createForm.password}
                    onChange={(e) => setCreateForm({...createForm, password: e.target.value})}
                    className="w-full rounded-lg border border-gray-300 p-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                  />
                  <p className="text-xs text-gray-500 mt-1">Min 8 chars, 1 uppercase, 1 number, 1 special character.</p>
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Assign Role</label>
                  <select 
                    value={createForm.role}
                    onChange={(e) => setCreateForm({...createForm, role: e.target.value})}
                    className="w-full rounded-lg border border-gray-300 p-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                  >
                    <option value="PLAYER">PLAYER</option>
                    <option value="TURF_OWNER">TURF_OWNER</option>
                    <option value="ORG_ADMIN">ORG_ADMIN</option>
                    <option value="SUPER_ADMIN">SUPER_ADMIN</option>
                  </select>
                </div>
                
                <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
                  <Button variant="outline" type="button" onClick={() => setShowCreateModal(false)} disabled={createLoading}>Cancel</Button>
                  <Button type="submit" loading={createLoading} variant="primary">Create User</Button>
                </div>
              </form>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

export default AdminUsersPage;
