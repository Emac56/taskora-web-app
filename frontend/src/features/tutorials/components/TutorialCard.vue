<script setup>
import { RouterLink } from 'vue-router'
import StatusBadge from '../../../components/ui/StatusBadge.vue'

defineProps({
  tutorial: { type: Object, required: true }
})
</script>

<template>
  <RouterLink
    :to="{ name: 'tutorial-detail', params: { id: tutorial.id } }"
    class="block rounded-xl border border-gray-200 bg-white p-5 transition-shadow hover:shadow-md"
  >
    <StatusBadge :status="tutorial.status" />
    <h3 class="mt-3 text-base font-semibold text-navy">{{ tutorial.title }}</h3>
    <p class="mt-1 line-clamp-2 text-sm text-gray-500">{{ tutorial.description }}</p>
  </RouterLink>
</template>
