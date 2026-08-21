<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>

<template>
  <div class="rounded-xl border border-dashed border-gray-300 bg-white px-6 py-14 text-center">
    <p class="text-base font-semibold text-navy">{{ title }}</p>
    <p v-if="description" class="mt-1 text-sm text-gray-500">{{ description }}</p>
    <slot />
  </div>
</template>
