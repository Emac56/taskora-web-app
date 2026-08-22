import { it, expect, vi } from 'vitest'
import http from './http'
import { replaceSteps } from './tutorialSteps.api'

vi.mock('./http', () => ({
  default: { put: vi.fn() }
}))

it('replaceSteps puts the full step list with id/stepNumber/instruction/imageUrl', async () => {
  const steps = [
    { id: 10, stepNumber: 2, instruction: 'Step A' },
    { id: null, stepNumber: 1, instruction: 'New step', imageUrl: 'https://x.png' }
  ]
  http.put.mockResolvedValueOnce({ data: [{ id: 10 }, { id: 20 }] })

  const result = await replaceSteps(1, steps)

  expect(http.put).toHaveBeenCalledWith('/tutorials/1/steps', {
    steps: [
      { id: 10, stepNumber: 2, instruction: 'Step A', imageUrl: null },
      { id: null, stepNumber: 1, instruction: 'New step', imageUrl: 'https://x.png' }
    ]
  })
  expect(result).toEqual([{ id: 10 }, { id: 20 }])
})
