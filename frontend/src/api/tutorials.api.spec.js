import { it, expect, vi } from 'vitest'
import http from './http'
import { getTutorialStats } from './tutorials.api'

vi.mock('./http', () => ({
  default: { get: vi.fn() }
}))

it('getTutorialStats fetches aggregate counts from a single endpoint', async () => {
  http.get.mockResolvedValueOnce({
    data: { totalTutorials: 5, publishedCount: 3, draftCount: 2, totalSteps: 17 }
  })

  const result = await getTutorialStats()

  expect(http.get).toHaveBeenCalledWith('/tutorials/stats')
  expect(result).toEqual({ totalTutorials: 5, publishedCount: 3, draftCount: 2, totalSteps: 17 })
})
