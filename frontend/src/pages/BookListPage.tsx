import { useEffect, useState } from 'react'
import { BookCard } from '../components/BookCard'
import type { Book } from '../types/Book'
import { fetchBooks } from '../api/books'
import { useApiClient } from '../api/ApiClientContext'

export function BookListPage() {
  const apiClient = useApiClient()
  const [books, setBooks] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchBooks(apiClient)
      .then((data) => setBooks(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [apiClient])

  if (loading) {
    return <p className="page-status">読み込み中...</p>
  }

  if (error) {
    return <p className="page-status page-status--error">{error}</p>
  }

  return (
    <section className="page">
      <h1>書籍一覧</h1>
      <p className="page__lead">蔵書の検索・貸出・返却ができます</p>
      <section className="book-list">
        {books.map((book) => (
          <BookCard key={book.id} book={book} />
        ))}
      </section>
    </section>
  )
}
