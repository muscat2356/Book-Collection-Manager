import { isAxiosError, type AxiosInstance } from "axios";
import type { Book } from "../types/Book";

export type CreateBookRequest = {
    title: string; author: string; isbn: string; initialCopyCount: number
  }
  export type UpdateBookRequest = {
    title: string; author: string; isbn: string
  }

function toErrorMessage(err: unknown): string {
    if(isAxiosError(err)) {
        if(err.response?.status === 403) return "権限がありません"
        const data = err.response?.data as {message?: string} | undefined
        return data?.message ?? err.message
    }
    if(err instanceof Error) return err.message
    return "不明なエラーが発生しました"
}

export async function fetchBooks(apiClient: AxiosInstance): Promise<Book[]> {
    try {
        const response = await apiClient.get<Book[]>("/api/books")
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
        
    }
}


export async function fetchBookById(apiClient:AxiosInstance, id:string): Promise<Book | null> {
    try {
        const response = await apiClient.get<Book>(`/api/books/${id}`)
        return response.data
    } catch (err) {
        if(isAxiosError(err) && err.response?.status === 404) return null
        throw new Error(toErrorMessage(err))
    }
}

export async function createBook(apiClient: AxiosInstance, data: CreateBookRequest): Promise<Book>{
    try {
        const response = await apiClient.post<Book>("/api/books", data)
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }
}

export async function updateBook(apiClient:AxiosInstance, id: string, data: UpdateBookRequest): Promise<Book> {
    try {
        const response = await apiClient.put<Book>(`/api/books/${id}`, data)
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }
    
}

export async function deleteBook(
    apiClient: AxiosInstance,
    id: string
  ): Promise<void> {
    try {
      await apiClient.delete(`/api/books/${id}`)
    } catch (err) {
      throw new Error(toErrorMessage(err))
    }
  }

  export async function addBookCopy(apiClient: AxiosInstance, bookId: string){
    try {
        const response = await apiClient.post<{ id: number; status: "AVAILABLE" | "LOANED" }>(
            `/api/books/${bookId}/copies`, {}
        )
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }
  }

  export async function deleteBookCopy(
    apiClient: AxiosInstance,
    bookId: string,
    copyId: number
  ): Promise<void> {
    try {
      await apiClient.delete(`/api/books/${bookId}/copies/${copyId}`)
    } catch (err) {
      throw new Error(toErrorMessage(err))
    }
  }
