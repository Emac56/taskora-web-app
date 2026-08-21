import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import AdminTutorialFormView from './AdminTutorialFormView.vue'
import * as tutorialsApi from '../../api/tutorials.api'
import * as stepsApi from '../../api/tutorialSteps.api'

vi.mock('../../api/tutorials.api')
vi.mock('../../api/tutorialSteps.api')

const push = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

const AdminLayoutStub = {
  template: '<div><slot name="title" /><slot /></div>'
}

function mountForm(props = {}) {
  return mount(AdminTutorialFormView, {
    props: { id: null, ...props },
    global: {
      stubs: {
        AdminLayout: AdminLayoutStub
      }
    }
  })
}

const removeButtons = (wrapper) =>
  wrapper.findAll('button').filter((b) => b.text() === 'Remove')

beforeEach(() => {
  vi.clearAllMocks()
})

describe('AdminTutorialFormView - create mode happy path', () => {
  it('creates the tutorial then its step, and navigates away', async () => {
    tutorialsApi.createTutorial.mockResolvedValue({ id: 99 })
    stepsApi.createStep.mockResolvedValue({ id: 501 })

    const wrapper = mountForm()

    await flushPromises()

    await wrapper.find('#title').setValue('New Tutorial')
    await wrapper.find('#description').setValue('Description here')
    await wrapper.findAll('textarea')[1].setValue('Do the first thing')

    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(tutorialsApi.createTutorial).toHaveBeenCalledWith({
      title: 'New Tutorial',
      description: 'Description here',
      status: 'DRAFT'
    })

    expect(stepsApi.createStep).toHaveBeenCalledWith(99, {
      stepNumber: 1,
      instruction: 'Do the first thing',
      imageUrl: null
    })

    expect(push).toHaveBeenCalledWith({
      name: 'admin-tutorials-list'
    })
  })
})

describe('AdminTutorialFormView - save flow risk from FE-002', () => {
  const existingSteps = [
    {
      id: 10,
      stepNumber: 1,
      instruction: 'Step A',
      imageUrl: null
    },
    {
      id: 11,
      stepNumber: 2,
      instruction: 'Step B',
      imageUrl: null
    },
    {
      id: 12,
      stepNumber: 3,
      instruction: 'Step C',
      imageUrl: null
    }
  ]

  function setUpEditMode() {
    tutorialsApi.getTutorialById.mockResolvedValue({
      id: 5,
      title: 'Existing',
      description: 'Existing desc',
      status: 'DRAFT'
    })

    stepsApi.getStepsByTutorialId.mockResolvedValue(existingSteps)

    tutorialsApi.updateTutorial.mockResolvedValue({
      id: 5
    })

    stepsApi.updateStep.mockResolvedValue({})
  }

  it('treats a 404 on delete as "already gone" and still saves the rest', async () => {
    setUpEditMode()

    stepsApi.deleteStep.mockRejectedValueOnce({
      response: {
        status: 404
      }
    })

    const wrapper = mountForm({ id: 5 })

    await flushPromises()

    await removeButtons(wrapper)[0].trigger('click')

    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(stepsApi.deleteStep).toHaveBeenCalledWith(10)

    expect(wrapper.text()).not.toContain(
      'Could not save this tutorial.'
    )

    expect(push).toHaveBeenCalledWith({
      name: 'admin-tutorials-list'
    })
  })

  it('does not re-delete a step that already succeeded when the user retries after a partial failure', async () => {
    setUpEditMode()

    let attempt = 1

    stepsApi.deleteStep.mockImplementation((id) => {
      if (id === 10) {
        return Promise.resolve()
      }

      if (id === 11) {
        return attempt === 1
          ? Promise.reject({
              response: {
                status: 500
              }
            })
          : Promise.resolve()
      }
    })

    const wrapper = mountForm({ id: 5 })

    await flushPromises()

    await removeButtons(wrapper)[0].trigger('click')

    await removeButtons(wrapper)[0].trigger('click')

    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(stepsApi.deleteStep).toHaveBeenCalledWith(10)
    expect(stepsApi.deleteStep).toHaveBeenCalledWith(11)

    expect(stepsApi.updateStep).not.toHaveBeenCalled()

    expect(wrapper.text()).toContain(
      'Could not save this tutorial.'
    )

    stepsApi.deleteStep.mockClear()

    attempt = 2

    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(stepsApi.deleteStep).toHaveBeenCalledTimes(1)

    expect(stepsApi.deleteStep).toHaveBeenCalledWith(11)

    expect(stepsApi.updateStep).toHaveBeenCalledWith(12, {
      stepNumber: 1,
      instruction: 'Step C',
      imageUrl: null
    })

    expect(push).toHaveBeenCalledWith({
      name: 'admin-tutorials-list'
    })
  })
})
