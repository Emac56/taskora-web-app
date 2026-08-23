<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import StatCard from '../../features/dashboard/components/StatCard.vue'
import { useAuthStore } from '../../stores/auth'
import { getTutorialStats } from '../../api/tutorials.api'

const authStore = useAuthStore()

const loading = ref(true)
const errorMessage = ref('')
const totalTutorials = ref(0)
const publishedCount = ref(0)
const draftCount = ref(0)
const totalSteps = ref(0)

onMounted(async () => {
  try {
    const stats = await getTutorialStats()
    totalTutorials.value = stats.totalTutorials
    publishedCount.value = stats.publishedCount
    draftCount.value = stats.draftCount
    totalSteps.value = stats.totalSteps
  } catch (error) {
    errorMessage.value = error.message || 'Could not load dashboard data.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <AdminLayout>
    <template #title>Dashboard</template>

    <p class="mb-6 text-sm text-gray-500">
      Welcome back, {{ authStore.user?.name || 'Admin' }}!
    </p>

    <p v-if="loading" class="text-sm text-gray-400">Loading dashboard...</p>
    <p v-else-if="errorMessage" class="text-sm font-medium text-red-600">{{ errorMessage }}</p>

    <div v-else class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <StatCard label="Total Tutorials" :value="totalTutorials" />
      <StatCard label="Published Tutorials" :value="publishedCount" accent />
      <StatCard label="Draft Tutorials" :value="draftCount" />
      <StatCard label="Total Steps" :value="totalSteps" />
    </div>
  </AdminLayout>
</template>
