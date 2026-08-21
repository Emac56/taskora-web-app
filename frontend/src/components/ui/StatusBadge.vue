<script setup>
defineProps({
  status: {
    type: String,
    required: true,
    validator: (value) => ['DRAFT', 'PUBLISHED'].includes(value)
  }
})
</script>

<template>
  <span
    class="inline-block rounded-full px-3 py-1 text-xs font-semibold"
    :class="status === 'PUBLISHED'
      ? 'bg-gold text-navy-dark'
      : 'bg-gray-200 text-gray-600'"
  >
    {{ status === 'PUBLISHED' ? 'Published' : 'Draft' }}
  </span>
</template>
