import request from './request'

// ── Auth ──────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (username: string, password: string) =>
    request.post('/v1/auth/login', { username, password }),
  logout: () => request.post('/v1/auth/logout'),
  refresh: (refreshToken: string) => request.post('/v1/auth/refresh', { refreshToken })
}

// ── User ──────────────────────────────────────────────────────────────────────
export const userApi = {
  page: (params: any) => request.get('/v1/users', { params }),
  getById: (id: number) => request.get(`/v1/users/${id}`),
  create: (data: any) => request.post('/v1/users', data),
  update: (id: number, data: any) => request.put(`/v1/users/${id}`, data),
  delete: (id: number) => request.delete(`/v1/users/${id}`),
  transfer: (userId: number, data: any) => request.post(`/v1/users/${userId}/transfer`, data),
  transferHistory: (userId: number, params: any) =>
    request.get(`/v1/users/${userId}/transfers`, { params })
}

// ── Org ───────────────────────────────────────────────────────────────────────
export const deptApi = {
  tree: () => request.get('/v1/depts/tree'),
  create: (data: any) => request.post('/v1/depts', data),
  update: (id: number, data: any) => request.put(`/v1/depts/${id}`, data),
  delete: (id: number) => request.delete(`/v1/depts/${id}`),
  export: () => request.get('/v1/depts/export', { responseType: 'blob' })
}

export const positionApi = {
  page: (params: any) => request.get('/v1/positions', { params }),
  create: (data: any) => request.post('/v1/positions', data),
  update: (id: number, data: any) => request.put(`/v1/positions/${id}`, data),
  delete: (id: number) => request.delete(`/v1/positions/${id}`),
  assignMenus: (id: number, menuIds: number[]) =>
    request.post(`/v1/positions/${id}/menus`, { menuIds })
}

export const menuApi = {
  tree: () => request.get('/v1/menus/tree'),
  create: (data: any) => request.post('/v1/menus', data),
  update: (id: number, data: any) => request.put(`/v1/menus/${id}`, data),
  delete: (id: number) => request.delete(`/v1/menus/${id}`)
}

// ── Workflow ──────────────────────────────────────────────────────────────────
export const processApi = {
  listLatest: () => request.get('/v1/workflow/definitions'),
  detail: (processKey: string, params?: any) =>
    request.get(`/v1/workflow/definitions/${processKey}`, { params }),
  deploy: (data: any) => request.post('/v1/workflow/definitions', data),
  togglePin: (id: number, pin: boolean) =>
    request.put(`/v1/workflow/definitions/${id}/pin`, null, { params: { pin } }),
  pinned: () => request.get('/v1/workflow/definitions/pinned'),
  startProcess: (data: any) => request.post('/v1/workflow/instances', data),
  myPendingTasks: () => request.get('/v1/workflow/tasks/my-pending'),
  approve: (taskId: string, data: any) => request.post(`/v1/workflow/tasks/${taskId}/approve`, data),
  reject: (taskId: string, data: any) => request.post(`/v1/workflow/tasks/${taskId}/reject`, data)
}

export const categoryApi = {
  list: () => request.get('/v1/workflow/categories'),
  create: (data: any) => request.post('/v1/workflow/categories', data),
  update: (id: number, data: any) => request.put(`/v1/workflow/categories/${id}`, data),
  delete: (id: number) => request.delete(`/v1/workflow/categories/${id}`)
}

// ── Attendance ────────────────────────────────────────────────────────────────
export const zoneApi = {
  list: () => request.get('/v1/attendance/zones'),
  create: (data: any) => request.post('/v1/attendance/zones', data),
  update: (id: number, data: any) => request.put(`/v1/attendance/zones/${id}`, data),
  delete: (id: number) => request.delete(`/v1/attendance/zones/${id}`)
}

export const ruleApi = {
  list: () => request.get('/v1/attendance/rules'),
  create: (data: any) => request.post('/v1/attendance/rules', data),
  update: (id: number, data: any) => request.put(`/v1/attendance/rules/${id}`, data),
  delete: (id: number) => request.delete(`/v1/attendance/rules/${id}`)
}

export const clockApi = {
  clock: (data: any) => request.post('/v1/attendance/clock', data),
  myRecords: (params: any) => request.get('/v1/attendance/records/my', { params }),
  allRecords: (params: any) => request.get('/v1/attendance/records', { params })
}

// ── Message ───────────────────────────────────────────────────────────────────
export const messageApi = {
  list: (params: any) => request.get('/v1/messages', { params }),
  markRead: (id: number) => request.put(`/v1/messages/${id}/read`),
  unreadCount: () => request.get('/v1/messages/unread-count')
}

// ── Gateway Blacklist/Whitelist ───────────────────────────────────────────────
export const ipListApi = {
  listBlacklist: (params: any) => request.get('/manage/blacklist', { params }),
  addBlacklist: (data: any) => request.post('/manage/blacklist', data),
  deleteBlacklist: (id: number) => request.delete(`/manage/blacklist/${id}`),
  listWhitelist: (params: any) => request.get('/manage/whitelist', { params }),
  addWhitelist: (data: any) => request.post('/manage/whitelist', data),
  deleteWhitelist: (id: number) => request.delete(`/manage/whitelist/${id}`)
}
