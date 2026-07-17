export type LoanStatus = 'BORROWED' | 'RETURNED'

export type Loan = {
  id: number
  bookCopyId: number
  bookId: number
  bookTitle?: string
  userId: number
  borrowedAt: string
  returnedAt: string | null
  status: LoanStatus
}
