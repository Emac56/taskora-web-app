import { describe, it, expect, vi, beforeEach } from 'vitest'
import http from './http'
import {
  getStepsByTutorialId,
  getStepById,
  createStep,
  updateStep,
  deleteStep,
  uploadStepImage
} from './tutorialSteps.api'

vi.mock('./http', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('tutorialSteps.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getStepsByTutorialId fetches steps for a specific tutorial', async () => {
    const mockData = [{ id: 1, stepNumber: 1, instruction: 'Step 1' }]
    http.get.mockResolvedValueOnce({ data: mockData })

    const result = await getStepsByTutorialId(10)

    expect(http.get).toHaveBeenCalledWith('/tutorials/10/steps')
    expect(result).toEqual(mockData)
  })

  it('getStepById fetches a single step by id', async () => {
    const mockData = { id: 5, stepNumber: 1, instruction: 'Step 1' }
    http.get.mockResolvedValueOnce({ data: mockData })

    const result = await getStepById(5)

    expect(http.get).toHaveBeenCalledWith('/tutorial-steps/5')
    expect(result).toEqual(mockData)
  })

  it('createStep posts step payload including imageUrl', async () => {
    const payload = {
      stepNumber: 1,
      instruction: 'Do this step',
      imageUrl: 'https://storage.example.com/image.png'
    }
    http.post.mockResolvedValueOnce({ data: { id: 101, ...payload } })

    const result = await createStep(1, payload)

    expect(http.post).toHaveBeenCalledWith('/tutorials/1/steps', {
      stepNumber: 1,
      instruction: 'Do this step',
      imageUrl: 'https://storage.example.com/image.png'
    })
    expect(result.id).toBe(101)
  })

  it('createStep falls back to null when imageUrl is undefined', async () => {
    http.post.mockResolvedValueOnce({ data: { id: 102 } })

    await createStep(1, { stepNumber: 2, instruction: 'No image step' })

    expect(http.post).toHaveBeenCalledWith('/tutorials/1/steps', {
      stepNumber: 2,
      instruction: 'No image step',
      imageUrl: null
    })
  })

  it('updateStep puts updated payload to step endpoint', async () => {
    const payload = {
      stepNumber: 2,
      instruction: 'Updated instruction',
      imageUrl: 'https://storage.example.com/updated.png'
    }
    http.put.mockResolvedValueOnce({ data: { id: 5, ...payload } })

    const result = await updateStep(5, payload)

    expect(http.put).toHaveBeenCalledWith('/tutorial-steps/5', payload)
    expect(result.instruction).toBe('Updated instruction')
  })

  it('deleteStep sends delete request by step id', async () => {
    http.delete.mockResolvedValueOnce({ status: 204 })

    await deleteStep(7)

    expect(http.delete).toHaveBeenCalledWith('/tutorial-steps/7')
  })

  it('uploadStepImage posts FormData with Content-Type undefined', async () => {
    const file = new File(['dummy-content'], 'test.png', { type: 'image/png' })
    http.post.mockResolvedValueOnce({
      data: { imageUrl: 'https://storage.example.com/test.png' }
    })

    const result = await uploadStepImage(file)

    expect(http.post).toHaveBeenCalledTimes(1)
    const [url, formData, config] = http.post.mock.calls[0]

    expect(url).toBe('/tutorial-steps/images')
    expect(formData).toBeInstanceOf(FormData)
    expect(formData.get('file')).toBe(file)
    expect(config).toEqual({ headers: { 'Content-Type': undefined } })
    expect(result).toEqual({ imageUrl: 'https://storage.example.com/test.png' })
  })
})
