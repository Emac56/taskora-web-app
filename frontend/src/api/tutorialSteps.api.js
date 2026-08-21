import http from './http'

// GET /api/v1/tutorials/{tutorialId}/steps -> TutorialStepResponse[]
export function getStepsByTutorialId(tutorialId) {
  return http.get(`/tutorials/${tutorialId}/steps`).then((res) => res.data)
}

// GET /api/v1/tutorial-steps/{id} -> TutorialStepResponse
export function getStepById(id) {
  return http.get(`/tutorial-steps/${id}`).then((res) => res.data)
}

// POST /api/v1/tutorials/{tutorialId}/steps (ADMIN)
// -> body: { stepNumber, instruction, imageUrl }
// imageUrl is optional - upload the file first via uploadStepImage(),
// then pass the returned URL here.
export function createStep(tutorialId, payload) {
  return http
    .post(`/tutorials/${tutorialId}/steps`, {
      stepNumber: payload.stepNumber,
      instruction: payload.instruction,
      imageUrl: payload.imageUrl ?? null
    })
    .then((res) => res.data)
}

// PUT /api/v1/tutorial-steps/{id} (ADMIN)
// -> body: { stepNumber, instruction, imageUrl }
export function updateStep(id, payload) {
  return http
    .put(`/tutorial-steps/${id}`, {
      stepNumber: payload.stepNumber,
      instruction: payload.instruction,
      imageUrl: payload.imageUrl ?? null
    })
    .then((res) => res.data)
}

// DELETE /api/v1/tutorial-steps/{id} (ADMIN)
export function deleteStep(id) {
  return http.delete(`/tutorial-steps/${id}`)
}

// POST /api/v1/tutorial-steps/images (ADMIN) -> { imageUrl }
// Uploads a single image file to Supabase Storage (via the backend) and
// returns its public URL. Call this first, then pass the returned imageUrl
// into createStep()/updateStep() above.
//
// IMPORTANT: our axios instance (http.js) sets a default
// 'Content-Type: application/json' header on ALL requests. That default
// silently overrides the multipart boundary the browser would normally
// set for FormData, which made the backend reject the request with
// "Current request is not a multipart request". Explicitly clearing the
// header here (per-request) lets the browser set the correct
// 'multipart/form-data; boundary=...' value instead.
export function uploadStepImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return http
    .post('/tutorial-steps/images', formData, {
      headers: { 'Content-Type': undefined }
    })
    .then((res) => res.data)
}
