import { useEffect, useState } from "react";
import { useApiClient } from "../api/ApiClientContext";
import type { Book } from "../types/Book";
import { fetchBooks } from "../api/books";
import { BookCard } from "../components/BookCard";

export function CheckoutBookListPage() {
  const apiClient = useApiClient()
  const [books, setBooks] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false
    fetchBooks(apiClient)
    .then((data) => {
      if(!ignore) setBooks(data)
    })
    .catch((err) => {
      if(!ignore) setError(err.message)
    })
    .finally(() => {
      if(!ignore) setLoading(false)
    })
  return () => {
    ignore = true
  }
  }, [apiClient])


  if (loading) return <p className="page-status">読み込み中…</p>
  if (error) return <p className="page-status page-status--error">{error}</p>

  return (
    <section className="book-list">
      {books.map((book) => (
        <BookCard
          key={book.id}
          book={book}
          to={`/loans/checkout/books/${book.id}`}
          linkLabel="所蔵を選ぶ →"
        />
      ))}
    </section>
  )
}
