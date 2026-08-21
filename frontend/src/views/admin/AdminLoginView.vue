<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import LoginForm from '../../features/auth/components/LoginForm.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')

// route.query.redirect is user-controlled (anyone can craft
// /admin/login?redirect=... ). Only accept it if it looks like a real
// internal admin path; otherwise fall back to the dashboard. This also
// guards against protocol-relative values like "//evil.com" (doesn't
// start with "/admin") and array values from duplicate query keys
// (typeof check rejects non-strings).
function resolveRedirectTarget(redirect) {
  if (typeof redirect === 'string' && redirect.startsWith('/admin')) {
    return redirect
  }
  return { name: 'admin-dashboard' }
}

async function handleLogin({ email, password }) {
  loading.value = true
  errorMessage.value = ''

  try {
    await authStore.login(email, password)
    router.push(resolveRedirectTarget(route.query.redirect))
  } catch (error) {
    errorMessage.value = error.message || 'Invalid email or password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gray-50 px-6">
    <div class="w-full max-w-sm rounded-2xl bg-white p-8 shadow-sm">
      <h1 class="mb-6 text-center text-xl font-bold text-navy">Admin Login</h1>
      <LoginForm :loading="loading" :error-message="errorMessage" @submit="handleLogin" />
    </div>
  </div>
</template>
