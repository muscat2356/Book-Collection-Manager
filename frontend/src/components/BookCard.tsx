import type { Book } from '../types/Book'
import { StockBadge } from './StockBadge'
import { BookCategory } from './BookCategory'
import { Link } from 'react-router-dom'

type BookCardProps = {
  book: Book
  to?: string
  linkLabel?: string
}

export function BookCard({ book, to, linkLabel = "詳細を見る →" }: BookCardProps) {

  const href = to ?? `/books/${book.id}`

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
          <dt>出版社</dt>
          <dd className="book-publisher">出版社：{book.publisher}</dd>
          <dt>カテゴリ</dt>
          <dd>
            <BookCategory category={book.category} />
          </dd>
        </dl>
        <Link to={href} className="book-card__link">
          {linkLabel}
        </Link>
      </div>
    </article>
  )
}
