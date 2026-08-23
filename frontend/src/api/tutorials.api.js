import http from './http'

// GET /api/v1/tutorials -> TutorialResponse[]
export function getAllTutorials() {
  return http.get('/tutorials').then((res) => res.data)
}

// GET /api/v1/tutorials/{id} -> TutorialResponse
export function getTutorialById(id) {
  return http.get(`/tutorials/${id}`).then((res) => res.data)
}

// GET /api/v1/tutorials/stats (ADMIN) -> { totalTutorials, publishedCount, draftCount, totalSteps }
export function getTutorialStats() {
  return http.get('/tutorials/stats').then((res) => res.data)
}

// POST /api/v1/tutorials (ADMIN) -> body: { title, description, status }
export function createTutorial(payload) {
  return http.post('/tutorials', payload).then((res) => res.data)
}

// PUT /api/v1/tutorials/{id} (ADMIN) -> body: { title, description, status }
export function updateTutorial(id, payload) {
  return http.put(`/tutorials/${id}`, payload).then((res) => res.data)
}

// DELETE /api/v1/tutorials/{id} (ADMIN)
export function deleteTutorial(id) {
  return http.delete(`/tutorials/${id}`)
}
