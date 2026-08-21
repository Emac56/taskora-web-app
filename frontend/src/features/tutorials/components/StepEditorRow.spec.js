import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import StepEditorRow from './StepEditorRow.vue'
import * as stepsApi from '../../../api/tutorialSteps.api'

vi.mock('../../../api/tutorialSteps.api')

function mountStepEditorRow(props = {}) {
  return mount(StepEditorRow, {
    props: {
      step: {
        id: 1,
        instruction: 'Sample instruction',
        imageUrl: null
      },
      index: 0,
      isFirst: false,
      isLast: false,
      ...props
    }
  })
}

describe('StepEditorRow.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders step index and instruction', () => {
    const wrapper = mountStepEditorRow({ index: 2 })

    expect(wrapper.text()).toContain('3') // index + 1
    const textarea = wrapper.find('textarea')
    expect(textarea.element.value).toBe('Sample instruction')
  })

  it('emits update:step when instruction textarea changes', async () => {
    const step = { id: 1, instruction: 'Original', imageUrl: null }
    const wrapper = mountStepEditorRow({ step })

    const textarea = wrapper.find('textarea')
    await textarea.setValue('Modified instruction')

    expect(wrapper.emitted('update:step')).toBeTruthy()
    expect(wrapper.emitted('update:step')[0][0]).toEqual({
      ...step,
      instruction: 'Modified instruction'
    })
  })

  it('shows "Add image" button when no image is uploaded', () => {
    const wrapper = mountStepEditorRow({
      step: { id: 1, instruction: 'Step', imageUrl: null }
    })

    expect(wrapper.text()).toContain('Add image')
    expect(wrapper.find('img').exists()).toBe(false)
  })

  it('shows preview and "Replace image" & "Remove image" buttons when image exists', () => {
    const wrapper = mountStepEditorRow({
      step: {
        id: 1,
        instruction: 'Step',
        imageUrl: 'https://storage.example.com/step.png'
      }
    })

    const img = wrapper.find('img')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toBe('https://storage.example.com/step.png')
    expect(wrapper.text()).toContain('Replace image')
    expect(wrapper.text()).toContain('Remove image')
  })

  it('gives the step image meaningful alt text instead of treating it as decorative', () => {
    const wrapper = mountStepEditorRow({
      index: 2,
      step: {
        id: 1,
        instruction: 'Step',
        imageUrl: 'https://storage.example.com/step.png'
      }
    })

    // alt="" tells assistive tech to skip the image entirely, but this is a
    // user-uploaded instructional photo tied to a specific step - it needs
    // to match the meaningful alt text used in the public StepDisplayItem.
    expect(wrapper.find('img').attributes('alt')).toBe('Step 3')
  })

  it('uploads selected image file and emits update:step with imageUrl', async () => {
    stepsApi.uploadStepImage.mockResolvedValueOnce({
      imageUrl: 'https://storage.example.com/uploaded.png'
    })

    const step = { id: 1, instruction: 'Step', imageUrl: null }
    const wrapper = mountStepEditorRow({ step })

    const file = new File(['content'], 'sample.png', { type: 'image/png' })
    const fileInput = wrapper.find('input[type="file"]')

    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      writable: true
    })

    await fileInput.trigger('change')
    await flushPromises()

    expect(stepsApi.uploadStepImage).toHaveBeenCalledWith(file)
    expect(wrapper.emitted('update:step')).toBeTruthy()
    expect(wrapper.emitted('update:step')[0][0]).toEqual({
      ...step,
      imageUrl: 'https://storage.example.com/uploaded.png'
    })
  })

  it('displays error message when uploadStepImage fails', async () => {
    stepsApi.uploadStepImage.mockRejectedValueOnce(
      new Error('Image must not exceed 5MB.')
    )

    const step = { id: 1, instruction: 'Step', imageUrl: null }
    const wrapper = mountStepEditorRow({ step })

    const file = new File(['content'], 'sample.png', { type: 'image/png' })
    const fileInput = wrapper.find('input[type="file"]')

    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      writable: true
    })

    await fileInput.trigger('change')
    await flushPromises()

    expect(wrapper.text()).toContain('Image must not exceed 5MB.')
    expect(wrapper.emitted('update:step')).toBeFalsy()
  })

  it('emits update:step with null imageUrl when "Remove image" is clicked', async () => {
    const step = {
      id: 1,
      instruction: 'Step',
      imageUrl: 'https://storage.example.com/old.png'
    }
    const wrapper = mountStepEditorRow({ step })

    const removeImageButton = wrapper
      .findAll('button')
      .find((b) => b.text() === 'Remove image')

    await removeImageButton.trigger('click')

    expect(wrapper.emitted('update:step')).toBeTruthy()
    expect(wrapper.emitted('update:step')[0][0]).toEqual({
      ...step,
      imageUrl: null
    })
  })

  it('emits remove, move-up, and move-down events', async () => {
    const wrapper = mountStepEditorRow()

    const moveUpButton = wrapper.find('button[title="Move up"]')
    const moveDownButton = wrapper.find('button[title="Move down"]')
    const removeButton = wrapper
      .findAll('button')
      .find((b) => b.text() === 'Remove')

    await moveUpButton.trigger('click')
    await moveDownButton.trigger('click')
    await removeButton.trigger('click')

    expect(wrapper.emitted('move-up')).toHaveLength(1)
    expect(wrapper.emitted('move-down')).toHaveLength(1)
    expect(wrapper.emitted('remove')).toHaveLength(1)
  })

  it('exposes accessible names for the icon-only reorder/remove controls', () => {
    const wrapper = mountStepEditorRow()

    const moveUpButton = wrapper.find('button[title="Move up"]')
    const moveDownButton = wrapper.find('button[title="Move down"]')
    const removeButton = wrapper
      .findAll('button')
      .find((b) => b.text() === 'Remove')

    // title alone isn't reliably announced by screen readers, so each
    // control needs its own explicit aria-label as the accessible name.
    expect(moveUpButton.attributes('aria-label')).toBe('Move step up')
    expect(moveDownButton.attributes('aria-label')).toBe('Move step down')
    expect(removeButton.attributes('aria-label')).toBe('Remove step')
  })
})
