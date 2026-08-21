<script setup>
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  await authStore.logout()
  router.push({ name: 'admin-login' })
}
</script>

<template>
  <div class="flex min-h-screen bg-gray-50">
    <aside class="flex w-60 shrink-0 flex-col bg-navy px-4 py-6">
      <RouterLink :to="{ name: 'admin-dashboard' }" class="mb-8 px-2 text-xl font-bold text-white">
        Taskora
      </RouterLink>

      <nav class="flex flex-1 flex-col gap-1">
        <RouterLink
          :to="{ name: 'admin-tutorials-list' }"
          class="rounded-lg px-3 py-2.5 text-sm font-medium text-white/90 hover:bg-white/10"
          active-class="bg-white/10 text-gold"
        >
          Tutorials
        </RouterLink>
      </nav>

      <button
        type="button"
        class="rounded-lg px-3 py-2.5 text-left text-sm font-medium text-white/90 hover:bg-white/10"
        @click="handleLogout"
      >
        Logout
      </button>
    </aside>

    <div class="flex-1">
      <header class="flex items-center justify-between border-b border-gray-200 bg-white px-8 py-4">
        <h1 class="text-lg font-semibold text-navy">
          <slot name="title">Admin</slot>
        </h1>
        <span v-if="authStore.user" class="text-sm text-gray-500">
          {{ authStore.user.name }}
        </span>
      </header>

      <main class="px-8 py-8">
        <slot />
      </main>
    </div>
  </div>
</template>
