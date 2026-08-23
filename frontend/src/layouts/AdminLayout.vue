<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const isSidebarOpen = ref(false)

function closeSidebar() {
  isSidebarOpen.value = false
}

async function handleLogout() {
  await authStore.logout()
  router.push({ name: 'admin-login' })
}
</script>

<template>
  <div class="flex min-h-screen bg-gray-50">
    <!-- Mobile backdrop: click to close, hidden on md+ where the sidebar is static -->
    <div
      v-if="isSidebarOpen"
      class="fixed inset-0 z-30 bg-black/50 md:hidden"
      @click="closeSidebar"
    />

    <aside
      class="fixed inset-y-0 left-0 z-40 flex w-60 shrink-0 -translate-x-full flex-col bg-navy px-4 py-6 transition-transform duration-200 md:relative md:translate-x-0"
      :class="{ 'translate-x-0': isSidebarOpen }"
    >
      <RouterLink :to="{ name: 'admin-dashboard' }" class="mb-8 px-2 text-xl font-bold text-white" @click="closeSidebar">
        Taskora
      </RouterLink>

      <nav class="flex flex-1 flex-col gap-1">
        <RouterLink
          :to="{ name: 'admin-tutorials-list' }"
          class="rounded-lg px-3 py-2.5 text-sm font-medium text-white/90 hover:bg-white/10"
          active-class="bg-white/10 text-gold"
          @click="closeSidebar"
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

    <div class="flex min-w-0 flex-1 flex-col">
      <header class="flex items-center justify-between border-b border-gray-200 bg-white px-4 py-4 md:px-8">
        <div class="flex items-center gap-3">
          <button
            type="button"
            class="rounded-lg p-2 text-navy hover:bg-gray-100 md:hidden"
            aria-label="Toggle sidebar"
            @click="isSidebarOpen = !isSidebarOpen"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <h1 class="text-lg font-semibold text-navy">
            <slot name="title">Admin</slot>
          </h1>
        </div>
        <span v-if="authStore.user" class="text-sm text-gray-500">
          {{ authStore.user.name }}
        </span>
      </header>

      <main class="px-4 py-8 md:px-8">
        <slot />
      </main>
    </div>
  </div>
</template>
