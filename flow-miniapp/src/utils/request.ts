const BASE_URL = 'https://your-api-domain.com/api'   // Replace with actual gateway URL

function getToken(): string {
  return uni.getStorageSync('access_token') || ''
}

function setToken(accessToken: string, refreshToken: string) {
  uni.setStorageSync('access_token', accessToken)
  uni.setStorageSync('refresh_token', refreshToken)
}

function clearToken() {
  uni.removeStorageSync('access_token')
  uni.removeStorageSync('refresh_token')
  uni.reLaunch({ url: '/pages/login/index' })
}

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      success(res: any) {
        const data = res.data
        if (res.statusCode === 401 || data?.code === 401) {
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          clearToken()
          return reject(data)
        }
        if (res.statusCode === 403 || data?.code === 403) {
          uni.showToast({ title: '无接口请求权限', icon: 'none' })
          return reject(data)
        }
        if (data?.code && data.code !== 200) {
          uni.showToast({ title: data.message || '操作失败', icon: 'none' })
          return reject(data)
        }
        resolve(data?.data !== undefined ? data.data : data)
      },
      fail(err) {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

export { setToken, clearToken, getToken }
