import { Link } from "react-router-dom"
import type { Book } from "../types/Book"
import { StockBadge } from "./StockBadge"
import { useAuth } from "../auth/AuthContext"
import { isAdmin } from "../auth/roles"

type BookDetailProps = {
    book: Book
}

export function BookDetail({ book } : BookDetailProps) {

    const {keycloak} = useAuth();
    const holdings = book.holdings ?? []

    return (
        <article className="book-detail">
            <h2>{book.title}</h2>
            <p><strong>著者：</strong>{book.author}</p>
            <p><strong>ISBN：</strong>{book.isbn}</p>
            <StockBadge
              availableCount={book.availableCount}
              totalCount={book.totalCount}
            />

            <h3>所蔵一覧</h3>
            {holdings.length === 0 ? (
            <p>所蔵がありません</p>
            ) : (
            <table>
                <thead>
                <tr><th>所蔵 ID</th><th>状態</th></tr>
                </thead>
                <tbody>
                {holdings.map((h) => (
                    <tr key={h.id}>
                    <td>#{h.id}</td>
                    <td>{h.status}</td>
                    </tr>
                ))}
                </tbody>
            </table>
            )}
            {isAdmin(keycloak) && (      
            <Link to={`/books/${book.id}/edit`} className="book-card__link">
                編集
            </Link>
            )}

        </article>
    )
}
