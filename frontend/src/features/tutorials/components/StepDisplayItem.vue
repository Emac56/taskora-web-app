<script setup>
defineProps({
  step: { type: Object, required: true }
})
</script>

<template>
  <li class="flex gap-4">
    <span
      class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-navy text-sm font-bold text-white"
    >
      {{ step.stepNumber }}
    </span>

    <div class="flex-1 pb-6">
      <p class="font-medium text-navy">{{ step.instruction }}</p>

      <img
        v-if="step.imageUrl"
        :src="step.imageUrl"
        :alt="`Step ${step.stepNumber}`"
        class="mt-3 w-full max-w-md rounded-lg border border-gray-200"
      />
      <div
        v-else
        class="mt-3 flex h-32 w-full max-w-md items-center justify-center rounded-lg bg-gray-100 text-xs text-gray-400"
      >
        No image for this step
      </div>
    </div>
  </li>
</template>
