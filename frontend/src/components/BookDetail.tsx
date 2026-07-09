import { Link, useNavigate } from "react-router-dom"
import type { Book } from "../types/Book"
import { StockBadge } from "./StockBadge"
import type { User } from "../types/User"
import type React from "react"
import { useState } from "react"
import { createLoan } from "../api/loans"
import { useAuth } from "../auth/AuthContext"
import { isAdmin } from "../auth/roles"

type BookDetailProps = {
    book: Book
    users: User[]
}

export function BookDetail({ book, users } : BookDetailProps) {

    const navigate = useNavigate();
    const {keycloak} = useAuth();
    const [selectedUserId, setSelectedUserId] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    function handleBorrow(e: React.MouseEvent<HTMLButtonElement>) {
        e.preventDefault()
        setError(null)
        setSubmitting(true)
    
        createLoan(book.id, selectedUserId)
        .then(() => navigate('/books'))
        .catch((err) => setError(err.message))
        .finally(() => setSubmitting(false))
    }

    return (
        <article className="book-detail">
            <h2>{book.title}</h2>
            <p><strong>著者：</strong>{book.author}</p>
            <p><strong>ISBN：</strong>{book.isbn}</p>
            <StockBadge stockCount={book.stockCount}/>

            <div className="book-card__actions">
                <select name="borrowUser"
                        id="borrowUser"
                        onChange={(e) => setSelectedUserId(e.target.value)}
                         value={selectedUserId}
                         >
                <option value="">選択してください</option>
                {users.filter(u => u.isActive).map(u => (
                    <option key={u.id} value={u.id}>{u.displayName}</option>
                ))}
                </select>
                <button
                 className="btn btn--primary"
                 onClick={handleBorrow}
                 disabled={!selectedUserId || book.stockCount === 0 || submitting}>{submitting ? "処理中..." : "借りる"}</button>
            </div>
            {error && (
                  <p className="page-status page-status--error">{error}</p>
                )}
            {isAdmin(keycloak) && (      
            <Link to={`/books/${book.id}/edit`} className="book-card__link">
                編集
            </Link>
            )}

        </article>
    )
}
