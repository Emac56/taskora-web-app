<script setup>
import { ref } from 'vue'
import { uploadStepImage } from '../../../api/tutorialSteps.api'

const props = defineProps({
  step: { type: Object, required: true },
  index: { type: Number, required: true },
  isFirst: { type: Boolean, default: false },
  isLast: { type: Boolean, default: false }
})

// Emit updates to the parent instead of directly mutating props
const emit = defineEmits(['remove', 'move-up', 'move-down', 'update:step'])

const fileInput = ref(null)
const uploading = ref(false)
const uploadError = ref('')

function triggerFilePicker() {
  fileInput.value?.click()
}

function updateField(field, value) {
  emit('update:step', {
    ...props.step,
    [field]: value
  })
}

async function handleFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = '' // allow re-selecting the same file later
  if (!file) return

  uploading.value = true
  uploadError.value = ''

  try {
    const result = await uploadStepImage(file)
    updateField('imageUrl', result.imageUrl)
  } catch (error) {
    uploadError.value = error.message || 'Could not upload image.'
  } finally {
    uploading.value = false
  }
}

function removeImage() {
  updateField('imageUrl', null)
}

function handleInstructionInput(event) {
  updateField('instruction', event.target.value)
}
</script>

<template>
  <div class="flex gap-3 rounded-lg border border-gray-200 bg-white p-4">
    <span
      class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-navy text-sm font-bold text-white"
    >
      {{ index + 1 }}
    </span>

    <div class="flex-1 space-y-3">
      <textarea
        :id="'step-instruction-' + index"
        :value="step.instruction"
        rows="2"
        required
        maxlength="5000"
        aria-label="Step instruction"
        placeholder="What should the reader do in this step?"
        class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-navy focus:outline-none focus:ring-1 focus:ring-navy"
        @input="handleInstructionInput"
      />

      <div class="flex items-center gap-3">
        <img
          v-if="step.imageUrl"
          :src="step.imageUrl"
          :alt="`Step ${index + 1}`"
          class="h-16 w-16 rounded-md border border-gray-200 object-cover"
        />

        <input
          :id="'step-image-file-' + index"
          ref="fileInput"
          type="file"
          accept="image/png,image/jpeg,image/webp"
          aria-label="Upload step image"
          class="hidden"
          @change="handleFileChange"
        />

        <button
          type="button"
          :disabled="uploading"
          class="text-xs font-medium text-navy hover:underline disabled:opacity-50"
          @click="triggerFilePicker"
        >
          {{ uploading ? 'Uploading...' : step.imageUrl ? 'Replace image' : 'Add image' }}
        </button>

        <button
          v-if="step.imageUrl && !uploading"
          type="button"
          class="text-xs font-medium text-red-500 hover:underline"
          @click="removeImage"
        >
          Remove image
        </button>
      </div>

      <p v-if="uploadError" class="text-xs font-medium text-red-600">{{ uploadError }}</p>
    </div>

    <div class="flex shrink-0 flex-col items-center gap-1">
      <button
        type="button"
        :disabled="isFirst"
        class="text-xs text-gray-400 hover:text-navy disabled:opacity-30"
        title="Move up"
        aria-label="Move step up"
        @click="emit('move-up')"
      >
        ▲
      </button>
      <button
        type="button"
        :disabled="isLast"
        class="text-xs text-gray-400 hover:text-navy disabled:opacity-30"
        title="Move down"
        aria-label="Move step down"
        @click="emit('move-down')"
      >
        ▼
      </button>
      <button
        type="button"
        class="mt-1 text-xs font-medium text-red-500 hover:underline"
        aria-label="Remove step"
        @click="emit('remove')"
      >
        Remove
      </button>
    </div>
  </div>
</template>
