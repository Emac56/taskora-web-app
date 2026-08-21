<script setup>
import { ref, onMounted } from 'vue'
import PublicLayout from '../../layouts/PublicLayout.vue'
import TutorialCard from '../../features/tutorials/components/TutorialCard.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import { getAllTutorials } from '../../api/tutorials.api'

const tutorials = ref([])
const loading = ref(true)
const errorMessage = ref('')

onMounted(async () => {
  try {
    const all = await getAllTutorials()
    // Visitors should only see published tutorials; drafts are admin-only.
    tutorials.value = all.filter((t) => t.status === 'PUBLISHED')
  } catch (error) {
    errorMessage.value = error.message || 'Could not load tutorials.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PublicLayout>
    <h1 class="text-2xl font-bold text-navy">Tutorials</h1>
    <p class="mt-1 text-sm text-gray-500">Step-by-step tutorials for your school tasks.</p>

    <p v-if="loading" class="mt-8 text-sm text-gray-400">Loading tutorials...</p>

    <p v-else-if="errorMessage" class="mt-8 text-sm font-medium text-red-600">
      {{ errorMessage }}
    </p>

    <EmptyState
      v-else-if="tutorials.length === 0"
      title="No tutorials yet"
      description="Check back later for new step-by-step guides."
      class="mt-8"
    />

    <div v-else class="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <TutorialCard v-for="tutorial in tutorials" :key="tutorial.id" :tutorial="tutorial" />
    </div>
  </PublicLayout>
</template>
