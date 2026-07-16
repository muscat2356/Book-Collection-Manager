
import { isAxiosError, type AxiosInstance } from "axios"
import type { Loan } from "../types/Loan"

export type CreateLoanRequest = {
    userId: number
    bookCopyIds: number[]
}

export type CreateLoanResponse = {
    loans: Loan[]
}

export type ActiveLoan = {
    id: number
    bookCopyId: number
    book: { id: number, title: string, author: string }
    user: { id: number, displayName: string }
    borrowedAt: string
    returnedAt: null
    status: 'BORROWED'
}

type LoanApiErrorBody = {
  message?: string
  error?: string
  failedBookCopyIds?: number[]
}

function toErrorMessage(err: unknown): string {
  if (isAxiosError(err)) {
    if (err.response?.status === 403) return "権限がありません"
    const data = err.response?.data as LoanApiErrorBody | undefined
    const message = data?.message ?? err.message

    // 409: 貸出不可の所蔵がある（全件ロールバック）
    if (err.response?.status === 409) {
      const ids = data?.failedBookCopyIds
      if (ids && ids.length > 0) {
        return `${message}（所蔵 #${ids.join(", #")}）`
      }
      return message || "貸出できない所蔵が含まれています"
    }

    return message
  }
  if (err instanceof Error) return err.message
  return "不明なエラーが発生しました"
}

export async function createLoan(
    apiClient: AxiosInstance,
    request: CreateLoanRequest): Promise<CreateLoanResponse>{
        
    try {
        const response = await apiClient.post<CreateLoanResponse>("/api/loans", request)
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }

}

export async function returnLoans(
    apiClient:AxiosInstance,
    loanId: number
):Promise<Loan> {
    try {
        const response = await apiClient.put<Loan>(`/api/loans/${loanId}/return`)
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }
}

export async function fetchActiveLoans(apiClient:AxiosInstance):Promise<ActiveLoan[]>
 {
    try {
        const response = await apiClient.get<ActiveLoan[]>("/api/loans/active")
        return response.data
    } catch (err) {
        throw new Error(toErrorMessage(err))
    }

}
