import type { Book } from '../types/Book'

// Book 型の配列 = List<Book> に相当
export const mockBooks: Book[] = [
  {
    id: 1,
    title: 'React入門',
    author: 'John Doe',
    isbn: '978-1234567890',
    totalCount: 10,
    availableCount: 10,
  },
  {
    id: 2,
    title: '達人プログラマー',
    author: 'Hunt / Thomas',
    isbn: '978-4-7741-5650-4',
    totalCount: 10,
    availableCount: 10,
  },
  {
    id: 3,
    title: 'Clean Architecture',
    author: 'Robert C. Martin',
    isbn: '978-4-274-25725-3',
    totalCount: 10,
    availableCount: 10,
  },
]
