import { Link, useParams } from 'react-router-dom'
import { useEffect, useState } from 'react'
import type { Book } from '../types/Book'
import { fetchBookById } from '../api/books'
import { BookDetail } from '../components/BookDetail'
import { useApiClient } from '../api/ApiClientContext'

export function BookDetailPage() {
  const { id } = useParams()
  const apiClient = useApiClient()
  const [book, setBook] = useState<Book | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) {
      setError('書籍IDが指定されていません')
      setLoading(false)
      return
    }

    fetchBookById(apiClient, id)
      .then((data) => {
        if(!data){
          setError("書籍が見つかりません")
          return
        }
        setBook(data)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))}, [id, apiClient])

  if (loading) {
    return <p className="page-status">読み込み中...</p>
  }

  if (error) {
    return <p className="page-status page-status--error">{error}</p>
  }

  if (!book) {
    return <p className="page-status page-status--error">書籍が見つかりません</p>
  }

  return (
    <section className="page">
      <h1>書籍詳細</h1>
      <p className="page__lead">書籍の詳細情報</p>
      <BookDetail book={book}/>
      <Link to="/books" className="back-link">
        ← 一覧へ戻る
      </Link>
    </section>
  )
}
