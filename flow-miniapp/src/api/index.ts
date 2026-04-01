import { request } from '@/utils/request'

export const authApi = {
  login: (username: string, password: string) =>
    request({ url: '/v1/auth/login', method: 'POST', data: { username, password } })
}

export const processApi = {
  listLatest:   ()         => request({ url: '/v1/workflow/definitions' }),
  listCategories: ()       => request({ url: '/v1/workflow/categories' }),
  startProcess: (data: any) => request({ url: '/v1/workflow/instances', method: 'POST', data }),
  getInstance:  (id: string) => request({ url: `/v1/workflow/instances/${id}` }),
  myPendingTasks: ()       => request({ url: '/v1/workflow/tasks/my-pending' }),
  approve: (taskId: string, data: any) =>
    request({ url: `/v1/workflow/tasks/${taskId}/approve`, method: 'POST', data }),
  reject: (taskId: string, data: any) =>
    request({ url: `/v1/workflow/tasks/${taskId}/reject`, method: 'POST', data })
}

export const attendanceApi = {
  clock:      (data: any)   => request({ url: '/v1/attendance/clock', method: 'POST', data }),
  myRecords:  (params: any) => request({ url: '/v1/attendance/records/my', data: params }),
  zones:      ()             => request({ url: '/v1/attendance/zones' }),
  rules:      ()             => request({ url: '/v1/attendance/rules' })
}

export const profileApi = {
  getMe:    ()        => request({ url: '/v1/users/me' }),
  updateMe: (data: any) => request({ url: '/v1/users/me', method: 'PUT', data })
}

export const messageApi = {
  list:       (params: any) => request({ url: '/v1/messages', data: params }),
  markRead:   (id: number)  => request({ url: `/v1/messages/${id}/read`, method: 'PUT' }),
  unreadCount: ()            => request({ url: '/v1/messages/unread-count' })
}
