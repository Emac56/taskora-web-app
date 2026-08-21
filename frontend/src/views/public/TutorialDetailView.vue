<script setup>
import { ref, onMounted } from 'vue'
import PublicLayout from '../../layouts/PublicLayout.vue'
import StatusBadge from '../../components/ui/StatusBadge.vue'
import StepDisplayItem from '../../features/tutorials/components/StepDisplayItem.vue'
import { getTutorialById } from '../../api/tutorials.api'
import { getStepsByTutorialId } from '../../api/tutorialSteps.api'

const props = defineProps({
  id: { type: [String, Number], required: true }
})

const tutorial = ref(null)
const steps = ref([])
const loading = ref(true)
const errorMessage = ref('')

onMounted(async () => {
  try {
    const [tutorialData, stepsData] = await Promise.all([
      getTutorialById(props.id),
      getStepsByTutorialId(props.id)
    ])

    // Visitors should only see published tutorials; drafts are admin-only
    // (same rule as TutorialsListView). A non-published tutorial reached via
    // direct/guessed URL is treated as not found rather than rendered.
    if (tutorialData.status !== 'PUBLISHED') {
      errorMessage.value = 'Tutorial not found.'
      return
    }

    tutorial.value = tutorialData
    steps.value = [...stepsData].sort((a, b) => a.stepNumber - b.stepNumber)
  } catch (error) {
    errorMessage.value = error.message || 'Could not load this tutorial.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PublicLayout>
    <RouterLink :to="{ name: 'tutorials-list' }" class="text-sm text-gray-500 hover:text-navy">
      ← Back to tutorials
    </RouterLink>

    <p v-if="loading" class="mt-6 text-sm text-gray-400">Loading tutorial...</p>
    <p v-else-if="errorMessage" class="mt-6 text-sm font-medium text-red-600">{{ errorMessage }}</p>

    <template v-else-if="tutorial">
      <div class="mt-4">
        <StatusBadge :status="tutorial.status" />
        <h1 class="mt-3 text-2xl font-bold text-navy">{{ tutorial.title }}</h1>
        <p class="mt-2 text-sm text-gray-600">{{ tutorial.description }}</p>
      </div>

      <h2 class="mt-8 mb-4 text-base font-semibold text-navy">Steps</h2>

      <p v-if="steps.length === 0" class="text-sm text-gray-400">
        No steps have been added to this tutorial yet.
      </p>
      <ol v-else class="space-y-0">
        <StepDisplayItem v-for="step in steps" :key="step.id" :step="step" />
      </ol>
    </template>
  </PublicLayout>
</template>
