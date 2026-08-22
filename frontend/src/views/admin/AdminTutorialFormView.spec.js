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
  it('creates the tutorial then replaces its steps in one call, and navigates away', async () => {
    tutorialsApi.createTutorial.mockResolvedValue({ id: 99 })
    stepsApi.replaceSteps.mockResolvedValue([{ id: 501, stepNumber: 1 }])

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

    expect(stepsApi.replaceSteps).toHaveBeenCalledTimes(1)
    expect(stepsApi.replaceSteps).toHaveBeenCalledWith(99, [
      { id: null, stepNumber: 1, instruction: 'Do the first thing', imageUrl: null }
    ])

    expect(push).toHaveBeenCalledWith({
      name: 'admin-tutorials-list'
    })
  })
})

describe('AdminTutorialFormView - FE-029 zero-step submission', () => {
  it('blocks submit and shows an error when the only step is removed before saving', async () => {
    const wrapper = mountForm()

    await flushPromises()

    expect(removeButtons(wrapper)).toHaveLength(1)

    await removeButtons(wrapper)[0].trigger('click')

    expect(wrapper.text()).toContain('No steps yet. Add at least one step below.')

    await wrapper.find('#title').setValue('New Tutorial')
    await wrapper.find('#description').setValue('Description here')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(tutorialsApi.createTutorial).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Add at least one step before saving.')
  })

  it('still allows saving once a step is added back after being cleared to zero', async () => {
    tutorialsApi.createTutorial.mockResolvedValue({ id: 99 })
    stepsApi.replaceSteps.mockResolvedValue([{ id: 501, stepNumber: 1 }])

    const wrapper = mountForm()

    await flushPromises()

    await removeButtons(wrapper)[0].trigger('click')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(tutorialsApi.createTutorial).not.toHaveBeenCalled()

    const addStepButton = wrapper
      .findAll('button')
      .find((b) => b.text() === '+ Add Step')
    await addStepButton.trigger('click')

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
    expect(wrapper.text()).not.toContain('Add at least one step before saving.')
  })
})

describe('AdminTutorialFormView - atomic save (replaces old per-request FE-002/FE-028 tests)', () => {
  const existingSteps = [
    { id: 10, stepNumber: 1, instruction: 'Step A', imageUrl: null },
    { id: 11, stepNumber: 2, instruction: 'Step B', imageUrl: null },
    { id: 12, stepNumber: 3, instruction: 'Step C', imageUrl: null }
  ]

  function setUpEditMode() {
    tutorialsApi.getTutorialById.mockResolvedValue({
      id: 5,
      title: 'Existing',
      description: 'Existing desc',
      status: 'DRAFT'
    })
    stepsApi.getStepsByTutorialId.mockResolvedValue(existingSteps)
    tutorialsApi.updateTutorial.mockResolvedValue({ id: 5 })
  }

  it('sends exactly one replaceSteps call for a 3-step tutorial (regression: used to be up to 6 sequential requests)', async () => {
    setUpEditMode()
    stepsApi.replaceSteps.mockResolvedValue(existingSteps)

    const wrapper = mountForm({ id: 5 })
    await flushPromises()

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(stepsApi.replaceSteps).toHaveBeenCalledTimes(1)
    expect(stepsApi.deleteStep).not.toHaveBeenCalled()
    expect(stepsApi.updateStep).not.toHaveBeenCalled()
    expect(stepsApi.createStep).not.toHaveBeenCalled()

    expect(push).toHaveBeenCalledWith({ name: 'admin-tutorials-list' })
  })

  it('omits a removed step from the payload instead of issuing a separate delete call', async () => {
    setUpEditMode()
    stepsApi.replaceSteps.mockResolvedValue(existingSteps.slice(1))

    const wrapper = mountForm({ id: 5 })
    await flushPromises()

    await removeButtons(wrapper)[0].trigger('click')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(stepsApi.replaceSteps).toHaveBeenCalledWith(5, [
      { id: 11, stepNumber: 1, instruction: 'Step B', imageUrl: null },
      { id: 12, stepNumber: 2, instruction: 'Step C', imageUrl: null }
    ])
    expect(stepsApi.deleteStep).not.toHaveBeenCalled()
  })

  it('reorders steps in a single call without a false 409 conflict (regression for "conflicts with existing data")', async () => {
    setUpEditMode()
    stepsApi.replaceSteps.mockResolvedValue(existingSteps)

    const wrapper = mountForm({ id: 5 })
    await flushPromises()

    await wrapper.find('[aria-label="Move step down"]').trigger('click')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(stepsApi.replaceSteps).toHaveBeenCalledWith(5, [
      { id: 11, stepNumber: 1, instruction: 'Step B', imageUrl: null },
      { id: 10, stepNumber: 2, instruction: 'Step A', imageUrl: null },
      { id: 12, stepNumber: 3, instruction: 'Step C', imageUrl: null }
    ])

    expect(wrapper.text()).not.toContain('Could not save this tutorial.')
    expect(push).toHaveBeenCalledWith({ name: 'admin-tutorials-list' })
  })

  it('shows a clear error and does not navigate away when replaceSteps fails', async () => {
    setUpEditMode()
    stepsApi.replaceSteps.mockRejectedValue({
      message: 'This operation conflicts with existing data.'
    })

    const wrapper = mountForm({ id: 5 })
    await flushPromises()

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('This operation conflicts with existing data.')
    expect(push).not.toHaveBeenCalled()
  })
})
