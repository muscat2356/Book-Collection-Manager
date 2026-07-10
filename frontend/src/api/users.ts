import { isAxiosError, type AxiosInstance } from "axios";
import type { User } from "../types/User";

export type CreateUserRequest = {
    displayName: string
    email: string
    temporaryPassword: string
}

export type UpdateUserRequest = {
    displayName: string
    email: string
}

function toErrorMessage(err: unknown): string {
    if(isAxiosError(err)){
        if(err.response?.status === 403) return "権限がありません"
        const data = err.response?.data as { message?: string | null}
        return data?.message ?? err.message
    }
    if(err instanceof Error) return err.message
    return "不明なエラーが発生しました"
}


export async function fetchUsers(apiClient: AxiosInstance) {
    try {
        const response = await apiClient.get<User[]>("/api/users")
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }
}

export async function fetchUserById(apiClient:AxiosInstance, id:number):Promise<User | null> {
    try {
        const response = await apiClient.get<User>(`/api/users/${id}`)
        return response.data
    } catch (err) {
        if(isAxiosError(err) && err.response?.status === 404) return null
        throw new Error(toErrorMessage(err))
    }
}

export async function createUser(apiClient:AxiosInstance, data: CreateUserRequest): Promise<User> {
    try {
        const response = await apiClient.post<User>(`/api/users/`,data)
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }
}

export async function updateUser(apiClient:AxiosInstance, id: number, data: UpdateUserRequest): Promise<User> {
    try {
        const response = await apiClient.put<User>(`/api/users/${id}`, data)
        return response.data
      } catch (err) {
        throw new Error(toErrorMessage(err))
    }
}
