import { isAxiosError, type AxiosInstance } from "axios";
import type { CategoryNode } from "../types/Category";

function toErrorMessage(err: unknown): string {
    if(isAxiosError(err)) {
        if(err.response?.status === 403) return "権限がありません"
        const data = err.response?.data as { message?: string } | undefined
        return data?.message ?? err.message
    }
    if(err instanceof Error) return err.message
    return "不明なエラーが発生しました"
}

export async function fetchCategoryTree(
    apiClient: AxiosInstance
): Promise<CategoryNode[]> {
    try {
        const response = await apiClient.get<CategoryNode[]>("/api/categories/tree")
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }
}
