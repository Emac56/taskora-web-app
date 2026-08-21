<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary', // primary (gold) | secondary (navy) | danger | ghost
    validator: (v) => ['primary', 'secondary', 'danger', 'ghost'].includes(v)
  },
  type: {
    type: String,
    default: 'button'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const variantClasses = {
  primary: 'bg-gold text-navy-dark hover:bg-gold-dark focus-visible:ring-gold',
  secondary: 'bg-navy text-white hover:bg-navy-light focus-visible:ring-navy',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus-visible:ring-red-600',
  ghost: 'bg-transparent text-navy hover:bg-navy/5 focus-visible:ring-navy'
}
</script>

<template>
  <button
    :type="type"
    :disabled="disabled"
    class="inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
    :class="variantClasses[variant]"
  >
    <slot />
  </button>
</template>
