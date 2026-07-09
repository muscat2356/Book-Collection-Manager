import axios from "axios";

export function createApiClient(
    getAccessToken: (minValiditySeconds?: number) => Promise<string | null>
) {
    const apiClient = axios.create({
        baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
    })

    apiClient.interceptors.request.use(async (config) => {
        const token = await getAccessToken(30)
        if (!token) return config
      
        // headers が AxiosHeaders（クラス）なら set を使う
        if (config.headers && typeof (config.headers as any).set === "function") {
          ;(config.headers as any).set("Authorization", `Bearer ${token}`)
          return config
        }
      
        // それ以外（undefined / plain object）なら新しいオブジェクトで確実に付与
        config.headers = {
          ...(config.headers as any),
          Authorization: `Bearer ${token}`,
        }
        return config
      })
    return apiClient
}
