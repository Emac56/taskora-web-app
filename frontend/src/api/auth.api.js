import http from './http'

// POST /api/v1/users/login -> LoginResponse { id, name, email, role }
export function login(email, password) {
  return http.post('/users/login', { email, password }).then((res) => res.data)
}

// POST /api/v1/users/logout -> 204 No Content
export function logout() {
  return http.post('/users/logout')
}
