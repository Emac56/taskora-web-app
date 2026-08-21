<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <header class="bg-navy">
      <div class="mx-auto flex max-w-5xl items-center px-6 py-4">
        <RouterLink :to="{ name: 'tutorials-list' }" class="text-xl font-bold text-white">
          Taskora
        </RouterLink>
      </div>
    </header>

    <main class="mx-auto max-w-5xl px-6 py-8">
      <slot />
    </main>
  </div>
</template>
