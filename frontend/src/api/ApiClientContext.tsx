import { createContext, useContext, useMemo, type ReactNode } from "react"
import type { AxiosInstance } from "axios"
import { useAuth } from "../auth/AuthContext"
import { createApiClient } from "./client"

const ApiClientContext = createContext<AxiosInstance | null>(null)

export function ApiClientProvider({ children }: { children: ReactNode }) {
  // ✅ ここは React コンポーネントなので、useAuth()（hook）を呼べる場所
  const { getAccessToken } = useAuth()

  // ✅ getAccessToken（ただの関数）を createApiClient に渡す
  // useMemo は「毎回 axios を作り直さない」ため
  const apiClient = useMemo(() => createApiClient(getAccessToken), [getAccessToken])

  return <ApiClientContext.Provider value={apiClient}>{children}</ApiClientContext.Provider>
}

export function useApiClient(): AxiosInstance {
  // ✅ ここも hook（useContext）を呼べる場所（custom hook）
  const apiClient = useContext(ApiClientContext)
  if (!apiClient) {
    throw new Error("ApiClientProvider が見つかりません")
  }
  return apiClient
}
