<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], required: true },
  accent: { type: Boolean, default: false }
})
</script>

<template>
  <div
    class="rounded-xl border border-gray-200 bg-white p-5"
    :class="accent ? 'border-gold bg-gold/10' : ''"
  >
    <p class="text-3xl font-bold text-navy">{{ value }}</p>
    <p class="mt-1 text-sm text-gray-500">{{ label }}</p>
  </div>
</template>
