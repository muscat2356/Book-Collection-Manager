import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import { KeycloakProvider } from './auth/AuthContext.tsx'
import { ApiClientProvider } from './api/ApiClientContext.tsx'

// 練習用 / フルテーマの切り替え（.env.development の VITE_USE_FULL_THEME）
if (import.meta.env.VITE_USE_FULL_THEME === 'true') {
  import('./styles/theme/index.css')
  import('./styles/theme/app.css')
} else {
  import('./styles/practice.css')
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <KeycloakProvider>
      <ApiClientProvider>
        <App />
      </ApiClientProvider>
    </KeycloakProvider>
  </StrictMode>,
)
