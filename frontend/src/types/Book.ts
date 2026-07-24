export type CopyStatus = 'AVAILABLE' | 'LOANED'
export type BookHolding = { id: number; status: CopyStatus }
export type BookCategoryPath = {
  smallId: number
  smallName: string
  mediumId: number
  mediumName: string
  largeId: number
  largeName: string
}
export type Book = {
  id: number
  title: string
  author: string
  isbn: string
  publisher: string
  totalCount: number
  availableCount: number
  holdings?: BookHolding[]
  category: BookCategoryPath | null
}
