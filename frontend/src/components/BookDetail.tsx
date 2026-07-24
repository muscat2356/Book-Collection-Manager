import { Link } from "react-router-dom"
import type { Book, CopyStatus } from "../types/Book"
import { StockBadge } from "./StockBadge"
import { BookCategory } from "./BookCategory"
import { useAuth } from "../auth/AuthContext"
import { isAdmin } from "../auth/roles"

type BookDetailProps = {
    book: Book
}

function holdingStatusLabel(status: CopyStatus): string {
    if (status === 'AVAILABLE') return '貸出可'
    return '貸出中'
}

export function BookDetail({ book } : BookDetailProps) {

    const {keycloak} = useAuth();
    const holdings = book.holdings ?? []

    return (
        <article className="book-detail">
            <h2>{book.title}</h2>
            <dl className="book-detail__meta">
              <div className="book-detail__row">
                <dt>著者</dt>
                <dd>{book.author}</dd>
              </div>
              <div className="book-detail__row">
                <dt>ISBN</dt>
                <dd>{book.isbn}</dd>
              </div>
              <div className="book-detail__row book-detail__row--publisher">
                <dt>出版社</dt>
                <dd className="book-publisher">{book.publisher}</dd>
              </div>
              <div className="book-detail__row book-detail__row--category">
                <dt>カテゴリ</dt>
                <dd>
                  <BookCategory category={book.category} />
                </dd>
              </div>
            </dl>
            <StockBadge
              availableCount={book.availableCount}
              totalCount={book.totalCount}
            />

            <section className="holdings">
              <h3 className="holdings__title">所蔵一覧</h3>
              {holdings.length === 0 ? (
                <p className="holdings__empty">所蔵がありません</p>
              ) : (
                <table className="holdings__table">
                  <thead>
                    <tr>
                      <th scope="col">所蔵 ID</th>
                      <th scope="col">状態</th>
                    </tr>
                  </thead>
                  <tbody>
                    {holdings.map((h) => (
                      <tr key={h.id}>
                        <td>#{h.id}</td>
                        <td>
                          <span
                            className={`holdings__status holdings__status--${h.status.toLowerCase()}`}
                          >
                            {holdingStatusLabel(h.status)}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>

            {isAdmin(keycloak) && (
            <Link to={`/books/${book.id}/edit`} className="book-card__link">
                編集
            </Link>
            )}

        </article>
    )
}
