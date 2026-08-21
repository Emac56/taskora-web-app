<script setup>
import { RouterLink } from 'vue-router'
import PublicLayout from '../../layouts/PublicLayout.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
</script>

<template>
  <PublicLayout>
    <EmptyState
      title="Page not found"
      description="The page you're looking for doesn't exist or may have been moved."
      class="mt-8"
    >
      <RouterLink :to="{ name: 'tutorials-list' }" class="mt-4 inline-block">
        <BaseButton variant="primary">Back to tutorials</BaseButton>
      </RouterLink>
    </EmptyState>
  </PublicLayout>
</template>
