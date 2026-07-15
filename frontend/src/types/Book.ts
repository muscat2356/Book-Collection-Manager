export type CopyStatus = 'AVAILABLE' | 'LOANED'
export type BookHolding = { id: number; status: CopyStatus }
export type Book = {
  id: number
  title: string
  author: string
  isbn: string
  totalCount: number
  availableCount: number
  holdings?: BookHolding[]
}
