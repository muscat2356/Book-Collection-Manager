
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

// 貸出処理専用のエラー。メッセージに加えて失敗した所蔵IDと HTTP ステータスを保持する
export class LoanError extends Error {
  status?: number
  errorCode?: string
  failedBookCopyIds?: number[]

  constructor(
    message: string,
    options?: { status?: number; failedBookCopyIds?: number[], errorCode?:string }
  ) {
    super(message)
    this.name = "LoanError"
    this.status = options?.status
    this.errorCode = options?.errorCode
    this.failedBookCopyIds = options?.failedBookCopyIds
  }
}

// axios エラーを LoanError に正規化する（文字列だけでなく failedBookCopyIds も残す）
function toLoanError(err: unknown): LoanError {
  if (isAxiosError(err)) {
    const status = err.response?.status
    if (status === 403) return new LoanError("権限がありません", { status })

    const data = err.response?.data as LoanApiErrorBody | undefined
    const errorCode = data?.error
    const message = data?.message ?? err.message

    // 409: 貸出不可の所蔵がある（全件ロールバック）
    if (status === 409) {

      if(errorCode === "BOOK_ALREADY_LOANED_BY_USER"){
        return new LoanError(message || "同じ書籍は一人一冊までです", {status, errorCode})
      }

      const ids = data?.failedBookCopyIds
      const text =
        ids && ids.length > 0
          ? `${message}（所蔵 #${ids.join(", #")}）`
          : message || "貸出できない所蔵が含まれています"
      return new LoanError(text, { status, errorCode, failedBookCopyIds: ids })
    }

    return new LoanError(message, { status, errorCode })
  }
  if (err instanceof Error) return new LoanError(err.message)
  return new LoanError("不明なエラーが発生しました")
}

export async function createLoan(
    apiClient: AxiosInstance,
    request: CreateLoanRequest): Promise<CreateLoanResponse>{
        
    try {
        const response = await apiClient.post<CreateLoanResponse>("/api/loans", request)
        return response.data
    } catch (err) {
        throw toLoanError(err)
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
        throw toLoanError(err)
    }
}

export async function fetchActiveLoans(apiClient:AxiosInstance):Promise<ActiveLoan[]>
 {
    try {
        const response = await apiClient.get<ActiveLoan[]>("/api/loans/active")
        return response.data
    } catch (err) {
        throw toLoanError(err)
    }

}
