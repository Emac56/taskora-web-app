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

// PUT /api/v1/tutorials/{tutorialId}/steps (ADMIN) -> body: { steps: [...] }
// NEW: atomically replaces the FULL step list for a tutorial in one DB
// transaction. Steps with an id are updated, steps with id=null are
// created, and any existing step NOT included in the array is deleted.
// Replaces the old create/update/delete-per-step save loop, which sent
// up to ~2x the step count as separate HTTP requests and could leave the
// tutorial in a half-saved state if one of those requests failed.
export function replaceSteps(tutorialId, steps) {
  return http
    .put(`/tutorials/${tutorialId}/steps`, {
      steps: steps.map((step) => ({
        id: step.id ?? null,
        stepNumber: step.stepNumber,
        instruction: step.instruction,
        imageUrl: step.imageUrl ?? null
      }))
    })
    .then((res) => res.data)
}

// POST /api/v1/tutorial-steps/images (ADMIN) -> { imageUrl }
export function uploadStepImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return http
    .post('/tutorial-steps/images', formData, {
      headers: { 'Content-Type': undefined }
    })
    .then((res) => res.data)
}
