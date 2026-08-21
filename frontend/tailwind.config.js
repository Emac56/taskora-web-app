/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          DEFAULT: '#0A2A66',
          light: '#123A85',
          dark: '#081F4D'
        },
        gold: {
          DEFAULT: '#FFC72C',
          dark: '#E6B219'
        }
      }
    }
  },
  plugins: []
}
