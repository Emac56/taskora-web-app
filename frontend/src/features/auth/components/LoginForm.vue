<script setup>
import { ref } from 'vue'
import BaseButton from '../../../components/ui/BaseButton.vue'

const emit = defineEmits(['submit'])

const email = ref('')
const password = ref('')
const showPassword = ref(false)

defineProps({
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
})

function handleSubmit() {
  emit('submit', { email: email.value, password: password.value })
}
</script>

<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <div>
      <label for="email" class="mb-1.5 block text-sm font-medium text-gray-700">Email</label>
      <input
        id="email"
        v-model="email"
        type="email"
        required
        placeholder="Enter your email"
        class="w-full rounded-lg border border-gray-300 px-3.5 py-2.5 text-sm focus:border-navy focus:outline-none focus:ring-1 focus:ring-navy"
      />
    </div>

    <div>
      <label for="password" class="mb-1.5 block text-sm font-medium text-gray-700">Password</label>
      <div class="relative">
        <input
          id="password"
          v-model="password"
          :type="showPassword ? 'text' : 'password'"
          required
          placeholder="Enter your password"
          class="w-full rounded-lg border border-gray-300 px-3.5 py-2.5 pr-10 text-sm focus:border-navy focus:outline-none focus:ring-1 focus:ring-navy"
        />
        <button
          type="button"
          class="absolute inset-y-0 right-3 text-xs font-medium text-gray-400 hover:text-gray-600"
          @click="showPassword = !showPassword"
        >
          {{ showPassword ? 'Hide' : 'Show' }}
        </button>
      </div>
    </div>

    <p v-if="errorMessage" class="text-sm font-medium text-red-600" role="alert">
      {{ errorMessage }}
    </p>

    <BaseButton type="submit" variant="secondary" :disabled="loading" class="w-full justify-center">
      {{ loading ? 'Logging in...' : 'Login' }}
    </BaseButton>
  </form>
</template>
