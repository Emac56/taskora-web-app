<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../../layouts/AdminLayout.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import StepEditorRow from '../../features/tutorials/components/StepEditorRow.vue'
import { getTutorialById, createTutorial, updateTutorial } from '../../api/tutorials.api'
import { getStepsByTutorialId, replaceSteps } from '../../api/tutorialSteps.api'

const props = defineProps({
  id: { type: [String, Number], default: null }
})

const router = useRouter()
const isEditMode = computed(() => props.id !== null && props.id !== undefined)

const title = ref('')
const description = ref('')
const status = ref('DRAFT')
const steps = ref([])

const loading = ref(isEditMode.value)
const saving = ref(false)
const errorMessage = ref('')

let nextLocalKey = 0
function blankStep() {
  nextLocalKey += 1
  return { localKey: nextLocalKey, id: null, instruction: '', imageUrl: null }
}

function addStep() {
  steps.value.push(blankStep())
}

function removeStep(index) {
  // No need to track removed ids anymore — the backend diffs the
  // payload against the DB and deletes whatever is missing, atomically.
  steps.value.splice(index, 1)
}

function moveStep(index, direction) {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= steps.value.length) return
  const updated = [...steps.value]
  ;[updated[index], updated[targetIndex]] = [updated[targetIndex], updated[index]]
  steps.value = updated
}

onMounted(async () => {
  if (!isEditMode.value) {
    steps.value = [blankStep()]
    return
  }

  try {
    const [tutorial, existingSteps] = await Promise.all([
      getTutorialById(props.id),
      getStepsByTutorialId(props.id)
    ])
    title.value = tutorial.title
    description.value = tutorial.description
    status.value = tutorial.status
    steps.value = [...existingSteps]
      .sort((a, b) => a.stepNumber - b.stepNumber)
      .map((s) => ({ localKey: (nextLocalKey += 1), id: s.id, instruction: s.instruction, imageUrl: s.imageUrl }))
  } catch (error) {
    errorMessage.value = error.message || 'Could not load this tutorial.'
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  if (steps.value.length === 0) {
    errorMessage.value = 'Add at least one step before saving.'
    return
  }

  saving.value = true
  errorMessage.value = ''

  const payload = { title: title.value, description: description.value, status: status.value }

  try {
    const tutorialId = isEditMode.value
      ? (await updateTutorial(props.id, payload)).id
      : (await createTutorial(payload)).id

    // ONE request for the whole step list — create/update/delete/reorder
    // all committed atomically by the backend. No more sequential
    // per-step loop, no more partial-save state.
    const stepsPayload = steps.value.map((step, index) => ({
      id: step.id,
      stepNumber: index + 1,
      instruction: step.instruction,
      imageUrl: step.imageUrl
    }))

    await replaceSteps(tutorialId, stepsPayload)

    router.push({ name: 'admin-tutorials-list' })
  } catch (error) {
    errorMessage.value = error.message || 'Could not save this tutorial.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <AdminLayout>
    <template #title>{{ isEditMode ? 'Edit Tutorial' : 'Create Tutorial' }}</template>

    <p v-if="loading" class="text-sm text-gray-400">Loading tutorial...</p>

    <form v-else class="max-w-2xl space-y-8" @submit.prevent="handleSubmit">
      <div class="space-y-5 rounded-xl border border-gray-200 bg-white p-6">
        <div>
          <label for="title" class="mb-1.5 block text-sm font-medium text-gray-700">Title</label>
          <input
            id="title"
            v-model="title"
            type="text"
            required
            maxlength="255"
            placeholder="Enter tutorial title"
            class="w-full rounded-lg border border-gray-300 px-3.5 py-2.5 text-sm focus:border-navy focus:outline-none focus:ring-1 focus:ring-navy"
          />
        </div>

        <div>
          <label for="description" class="mb-1.5 block text-sm font-medium text-gray-700">Description</label>
          <textarea
            id="description"
            v-model="description"
            rows="3"
            required
            maxlength="5000"
            placeholder="Enter tutorial description"
            class="w-full rounded-lg border border-gray-300 px-3.5 py-2.5 text-sm focus:border-navy focus:outline-none focus:ring-1 focus:ring-navy"
          />
        </div>

        <div>
          <label for="status" class="mb-1.5 block text-sm font-medium text-gray-700">Status</label>
          <select
            id="status"
            v-model="status"
            class="w-full rounded-lg border border-gray-300 px-3.5 py-2.5 text-sm focus:border-navy focus:outline-none focus:ring-1 focus:ring-navy"
          >
            <option value="DRAFT">Draft</option>
            <option value="PUBLISHED">Published</option>
          </select>
        </div>
      </div>

      <div>
        <div class="mb-3 flex items-center justify-between">
          <h2 class="text-sm font-semibold text-navy">Steps</h2>
          <BaseButton type="button" variant="ghost" @click="addStep">+ Add Step</BaseButton>
        </div>

        <div class="space-y-3">
          <StepEditorRow
            v-for="(step, index) in steps"
            :key="step.localKey"
            :step="step"
            :index="index"
            :is-first="index === 0"
            :is-last="index === steps.length - 1"
            @update:step="steps[index] = $event"
            @remove="removeStep(index)"
            @move-up="moveStep(index, -1)"
            @move-down="moveStep(index, 1)"
          />
        </div>

        <p v-if="steps.length === 0" class="text-sm text-gray-400">
          No steps yet. Add at least one step below.
        </p>
      </div>

      <p v-if="errorMessage" class="text-sm font-medium text-red-600">{{ errorMessage }}</p>

      <div class="flex gap-3">
        <BaseButton type="submit" variant="primary" :disabled="saving">
          {{ saving ? 'Saving...' : 'Save' }}
        </BaseButton>
        <BaseButton type="button" variant="ghost" @click="router.push({ name: 'admin-tutorials-list' })">
          Cancel
        </BaseButton>
      </div>
    </form>
  </AdminLayout>
</template>
