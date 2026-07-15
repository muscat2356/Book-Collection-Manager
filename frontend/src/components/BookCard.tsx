import type { Book } from '../types/Book'
import { StockBadge } from './StockBadge'
import { Link } from 'react-router-dom'

type BookCardProps = {
  book: Book
}

export function BookCard({ book }: BookCardProps) {

  return (
    <article className="book-card">
      <div className="book-card__body">
        <div className="book-card__header">
          <h2 className="book-card__title">{book.title}</h2>
        </div>
        <StockBadge
          availableCount={book.availableCount}
          totalCount={book.totalCount}
        />
        <dl className="book-card__meta">
          <dt>著者</dt>
          <dd>著者：{book.author}</dd>
          <dt>ISBN</dt>
          <dd>ISBN：{book.isbn}</dd>
        </dl>
        <Link to={`/books/${book.id}`} className="book-card__link">
          詳細を見る →
        </Link>
      </div>
    </article>
  )
}
