<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import AdminLayout from '../../layouts/AdminLayout.vue'
import StatusBadge from '../../components/ui/StatusBadge.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import { getAllTutorials, deleteTutorial } from '../../api/tutorials.api'

const tutorials = ref([])
const loading = ref(true)
const errorMessage = ref('')
const deletingId = ref(null)

async function loadTutorials() {
  loading.value = true
  errorMessage.value = ''
  try {
    tutorials.value = await getAllTutorials()
  } catch (error) {
    errorMessage.value = error.message || 'Could not load tutorials.'
  } finally {
    loading.value = false
  }
}

async function handleDelete(tutorial) {
  const confirmed = window.confirm(`Delete "${tutorial.title}"? This also removes its steps.`)
  if (!confirmed) return

  deletingId.value = tutorial.id
  try {
    await deleteTutorial(tutorial.id)
    tutorials.value = tutorials.value.filter((t) => t.id !== tutorial.id)
  } catch (error) {
    errorMessage.value = error.message || 'Could not delete this tutorial.'
  } finally {
    deletingId.value = null
  }
}

onMounted(loadTutorials)
</script>

<template>
  <AdminLayout>
    <template #title>Tutorials</template>

    <div class="mb-5 flex items-center justify-between">
      <p v-if="errorMessage" class="text-sm font-medium text-red-600">{{ errorMessage }}</p>
      <span v-else></span>

      <RouterLink :to="{ name: 'admin-tutorial-create' }">
        <BaseButton variant="primary">+ New Tutorial</BaseButton>
      </RouterLink>
    </div>

    <p v-if="loading" class="text-sm text-gray-400">Loading tutorials...</p>

    <EmptyState
      v-else-if="tutorials.length === 0"
      title="No tutorials yet"
      description="Create your first tutorial to get started."
    />

    <div v-else class="overflow-hidden rounded-xl border border-gray-200 bg-white">
      <table class="w-full text-left text-sm">
        <thead class="bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
          <tr>
            <th class="px-5 py-3">ID</th>
            <th class="px-5 py-3">Title</th>
            <th class="px-5 py-3">Status</th>
            <th class="px-5 py-3 text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="tutorial in tutorials" :key="tutorial.id">
            <td class="px-5 py-4 text-gray-500">{{ tutorial.id }}</td>
            <td class="px-5 py-4 font-medium text-navy">{{ tutorial.title }}</td>
            <td class="px-5 py-4"><StatusBadge :status="tutorial.status" /></td>
            <td class="px-5 py-4">
              <div class="flex justify-end gap-3">
                <RouterLink
                  :to="{ name: 'admin-tutorial-edit', params: { id: tutorial.id } }"
                  class="text-sm font-medium text-navy hover:underline"
                >
                  Edit
                </RouterLink>
                <button
                  type="button"
                  class="text-sm font-medium text-red-600 hover:underline disabled:opacity-50"
                  :disabled="deletingId === tutorial.id"
                  @click="handleDelete(tutorial)"
                >
                  {{ deletingId === tutorial.id ? 'Deleting...' : 'Delete' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>
