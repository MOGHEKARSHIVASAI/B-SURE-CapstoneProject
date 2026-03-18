/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        // ── Burgundy scale (900 = darkest → 50 = ghostly)
        burg: {
          900: '#1E0309',
          800: '#3B0915',
          700: '#5C1227',
          600: '#7A1C35',
          500: '#982340',
          400: '#B83B57',
          300: '#CC6678',
          200: '#E0A0AE',
          100: '#F2D5DB',
          50: '#FBF0F2',
        },
        // ── Cream / warm neutral scale
        cream: {
          900: '#2B2320',
          800: '#4A3D38',
          700: '#6B5B53',
          600: '#8C7A70',
          500: '#A8958A',
          400: '#C4B0A5',
          300: '#D8CAC0',
          200: '#EAE0D8',
          100: '#F3EDE5',
          50: '#FAF6F0',
        },
        // ── Black / near black
        ink: {
          900: '#0A0806',
          800: '#1A120E',
          700: '#2D1E18',
          600: '#3F2D24',
          500: '#5A4038',
        },
        // ── Semantic quick-aliases (kept for template use)
        primary: '#7A1C35',
        surface: '#FFFFFF',
        bg: '#FAF6F0',
        // Legacy template compat aliases
        burgundy: '#7A1C35',
        'burgundy-dark': '#3B0915',
        'burgundy-light': '#B83B57',
        'burgundy-pale': '#F2D5DB',
        'cream-light': '#FAF6F0',
        'cream-dark': '#EAE0D8',
      },
      fontFamily: {
        display: ['Space Grotesk', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        sans: ['Inter', 'sans-serif'],
      },
      boxShadow: {
        card: '0 4px 18px rgba(26,18,14,0.09)',
        burg: '0 4px 18px rgba(122,28,53,0.25)',
        'burg-lg': '0 8px 28px rgba(122,28,53,0.32)',
      },
      borderRadius: {
        DEFAULT: '10px',
        lg: '16px',
        xl: '22px',
      },
    },
  },
  plugins: [],
}
