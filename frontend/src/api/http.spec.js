import { describe, it, expect, beforeEach, vi } from 'vitest'

// Every other *.api.spec.js mocks this module entirely — they only care
// that the right method/url/payload was called. This file is the
// exception: it tests http.js's own interceptor wiring, so it needs the
// real axios instance. We swap axios's own `adapter` extension point
// (a plain function) instead of pulling in a mocking library, so no
// request ever actually leaves the process.
function mockAdapter(responses) {
  let call = 0
  return (config) => {
    const response = responses[call] ?? responses[responses.length - 1]
    call += 1
    return Promise.resolve({
      data: response.data ?? {},
      status: response.status ?? 200,
      statusText: 'OK',
      headers: response.headers ?? {},
      config
    })
  }
}

// axios normalizes config.headers into an AxiosHeaders instance in some
// versions/paths and leaves it as a plain object in others depending on
// where in the pipeline you inspect it — read defensively so this test
// isn't coupled to which one axios 1.7.x happens to hand back here.
function getHeader(headers, name) {
  if (headers && typeof headers.get === 'function') {
    return headers.get(name)
  }
  return headers?.[name]
}

describe('http (CSRF token capture/attach)', () => {
  beforeEach(() => {
    // http.js keeps the captured token in a module-scoped variable, so
    // each test needs a fresh module instance — otherwise a token
    // captured in one test leaks into the next.
    vi.resetModules()
  })

  it('does not attach X-XSRF-TOKEN before any response has provided one', async () => {
    const { default: http } = await import('./http')
    http.defaults.adapter = mockAdapter([{ headers: {} }])

    const response = await http.get('/tutorials')

    expect(getHeader(response.config.headers, 'X-XSRF-TOKEN')).toBeUndefined()
  })

  it('captures X-XSRF-TOKEN from a response header and attaches it to the next request', async () => {
    const { default: http } = await import('./http')
    http.defaults.adapter = mockAdapter([
      { headers: { 'x-xsrf-token': 'token-abc' } },
      { headers: {} }
    ])

    await http.get('/tutorials')
    const secondResponse = await http.post('/tutorials', {})

    expect(getHeader(secondResponse.config.headers, 'X-XSRF-TOKEN')).toBe('token-abc')
  })

  it('keeps using the last captured token when a later response omits the header', async () => {
    const { default: http } = await import('./http')
    http.defaults.adapter = mockAdapter([
      { headers: { 'x-xsrf-token': 'token-abc' } },
      { headers: {} },
      { headers: {} }
    ])

    await http.get('/tutorials')
    await http.post('/tutorials', {})
    const thirdResponse = await http.put('/tutorials/1', {})

    expect(getHeader(thirdResponse.config.headers, 'X-XSRF-TOKEN')).toBe('token-abc')
  })

  it('updates to a newer token when the backend rotates it', async () => {
    const { default: http } = await import('./http')
    http.defaults.adapter = mockAdapter([
      { headers: { 'x-xsrf-token': 'token-abc' } },
      { headers: { 'x-xsrf-token': 'token-xyz' } },
      { headers: {} }
    ])

    await http.get('/tutorials')
    await http.post('/tutorials', {})
    const thirdResponse = await http.put('/tutorials/1', {})

    expect(getHeader(thirdResponse.config.headers, 'X-XSRF-TOKEN')).toBe('token-xyz')
  })
})
                                          
